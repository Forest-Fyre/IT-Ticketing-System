package org.seng2050.A3;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class ErrorController {

    // this is the only error that maps to login
    @GetMapping("/error_login")
    public String getLoginError(Model model) {
        String default_error = "Invalid login cridentials";
        model.addAttribute("error", default_error);
        return "login";
    }

    //everything else should map to the error page with an error param
    @GetMapping("/error")
    public String getError(@RequestParam String error, Model model,
    @AuthenticationPrincipal UserDetails userDetails) {

        model.addAttribute("username", userDetails.getUsername());
        String roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(", "));
        model.addAttribute("roles", roles);
        // role attribute checks
        boolean isManager = roles.contains("MANAGER");
		boolean isIt = roles.contains("IT");
		boolean isUser = roles.contains("USER");
        model.addAttribute("isManager", isManager);
        model.addAttribute("isUser", isUser);
        model.addAttribute("isIt", isIt);
        model.addAttribute("error", error);
        return "error";
    }
  

}
