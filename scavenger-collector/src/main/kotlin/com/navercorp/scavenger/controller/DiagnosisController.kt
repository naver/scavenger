package com.navercorp.scavenger.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.HandlerMapping
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.streams.toList

@RequestMapping("/diag")
@Controller
class DiagnosisController(
    @Value("\${scavenger.diagnosis-directory:}") val diagDirectory: String,
) {
    @ResponseBody
    @GetMapping("/**")
    fun list(request: HttpServletRequest): String {
        if (diagDirectory.isBlank()) {
            return "diagnosis is not allowed"
        }
        val path = extractFilePath(request).let {
            if (it.isNullOrBlank()) {
                it
            } else {
                "$it/"
            }
        }
        val files = Files.list(Paths.get("$diagDirectory/$path")).sorted().toList()

        val filesHtml = files.joinToString(separator = "\n") {
            if (it.isDirectory()) {
                """<li><a href="/diag/$path${it.name}">${it.name}</a> - ${
                    LocalDateTime.ofInstant(
                        it.getLastModifiedTime().toInstant(),
                        ZoneId.systemDefault()
                    )
                }</li>"""
            } else {
                """<li><a href="/diag/$path${it.name}?download">${it.name}</a> - ${
                    LocalDateTime.ofInstant(
                        it.getLastModifiedTime().toInstant(),
                        ZoneId.systemDefault()
                    )
                }</li>"""
            }
        }
        return """
            <html>
            <header>
            <style>
             li { }
            </style>
            </header>
            <body>
            <h2>current : $path</h2>
        ${
            if (path.isNullOrBlank()) "" else {
                """<a href="/diag/$path../">UP</a>"""
            }
        }
            <ul>
            $filesHtml
            </ul>
            </body>
            </html>
        """.trimIndent()
    }

    @ResponseBody
    @GetMapping("/**", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE], params = ["download"])
    fun download(request: HttpServletRequest, response: HttpServletResponse): FileSystemResource {
        val path = extractFilePath(request)
        response.setHeader("Content-Disposition", "attachment; filename=$path")
        return FileSystemResource("$diagDirectory/$path")
    }

    fun extractFilePath(request: HttpServletRequest): String? {
        val path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) as String
        val bestMatchPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as String
        val apm = AntPathMatcher()
        return apm.extractPathWithinPattern(bestMatchPattern, path)
    }
}
