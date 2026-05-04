package org.terrevivante.tvjournalists.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class SpaForwardingController {

    @ResponseBody
    @GetMapping(
        value = {"/", "/login", "/guide", "/admin/users", "/admin/themes", "/journalists/{id}"},
        produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<Resource> serveSpaBShell() {
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(new ClassPathResource("static/index.html"));
    }
}
