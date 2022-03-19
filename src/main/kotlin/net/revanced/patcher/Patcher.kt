package net.revanced.patcher

import net.revanced.patcher.cache.Cache
import net.revanced.patcher.patch.Patch
import net.revanced.patcher.patch.PatchResult
import net.revanced.patcher.resolver.MethodResolver
import net.revanced.patcher.signature.Signature
import net.revanced.patcher.util.Jar2ASM
import java.io.InputStream
import java.util.jar.JarFile

/**
 * The patcher. (docs WIP)
 *
 * @param input the input stream to read from, must be a JAR file (for now)
 */
class Patcher (
    input: InputStream,
    signatures: Array<Signature>,
) {
    val cache = Cache()
    private val patches: MutableList<Patch> = mutableListOf()

    init {
        cache.methods.putAll(MethodResolver(Jar2ASM.jar2asm(input), signatures).resolve())
    }

    fun addPatches(vararg patches: Patch) {
        this.patches.addAll(patches)
    }

    fun executePatches(): Map<String, Result<Nothing?>> {
        return buildMap {
            for (patch in patches) {
                val result: Result<Nothing?> = try {
                    val pr = patch.execute()
                    if (pr.isSuccess()) continue
                    Result.failure(Exception(pr.error()?.errorMessage() ?: "Unknown error"))
                } catch (e: Exception) {
                    Result.failure(e)
                }
                this[patch.patchName] = result
            }
        }
    }
}