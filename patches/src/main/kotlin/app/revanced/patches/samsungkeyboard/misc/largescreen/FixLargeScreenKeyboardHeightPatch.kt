package app.revanced.patches.samsungkeyboard.misc.largescreen

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.revanced.patches.samsungkeyboard.misc.nononeui.enableNonOneUiPatch
import app.revanced.patches.samsungkeyboard.shared.Constants.COMPATIBILITY_SAMSUNG_KEYBOARD
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val COMPAT = "Lapp/revanced/extension/samsungkeyboard/KeyboardSizeCompat;"

@Suppress("unused")
val fixLargeScreenKeyboardHeightPatch = bytecodePatch(
    name = "Fix keyboard height on large screens",
    description = "Uses the Galaxy Z Fold keyboard height on the inner screen of non-Samsung foldables.",
) {
    compatibleWith(COMPATIBILITY_SAMSUNG_KEYBOARD)
    dependsOn(enableNonOneUiPatch)

    execute {
        val method = KeyboardSizeFractionFingerprint.method
        val registerCount = method.implementation?.registerCount
            ?: throw PatchException("Method has no implementation.")
        val firstParam = registerCount - method.parameters.size
        val idReg = firstParam + 1
        val basisReg = firstParam + 3

        val returnIndex = method.indexOfFirstInstructionOrThrow(Opcode.RETURN)
        val resultReg = method.getInstruction<OneRegisterInstruction>(returnIndex).registerA

        (0 until returnIndex).forEach { i ->
            val ins = method.getInstruction<Instruction>(i)
            if (ins is OneRegisterInstruction && ins.opcode.setsRegister() &&
                (ins.registerA == idReg || ins.registerA == basisReg)
            ) throw PatchException("Method overwrites its parameters.")
        }

        method.addInstructions(
            returnIndex,
            """
                invoke-static {v$idReg, v$basisReg, v$resultReg}, $COMPAT->adjustSize(IFF)F
                move-result v$resultReg
            """.trimIndent(),
        )
    }
}
