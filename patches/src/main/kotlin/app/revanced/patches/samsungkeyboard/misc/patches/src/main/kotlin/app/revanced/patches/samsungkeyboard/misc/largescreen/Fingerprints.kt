package app.revanced.patches.samsungkeyboard.misc.largescreen

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

internal object KeyboardSizeFractionFingerprint : Fingerprint(
    isStatic = true,
    returnType = "F",
    parameters = listOf("L", "I", "Ljava/lang/String;", "F"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/res/Resources;",
            name = "getString",
            parameters = listOf("I"),
            returnType = "Ljava/lang/String;",
        ),
        methodCall(
            definingClass = "Ljava/lang/Float;",
            name = "parseFloat",
            parameters = listOf("Ljava/lang/String;"),
            returnType = "F",
        ),
    ),
)
