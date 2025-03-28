package net.sys.gest.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sys.gest.domain.User;
import net.sys.gest.repository.UserRepository;

@Component
public class SecurityFilter extends OncePerRequestFilter {

	@Autowired
    TokenService tokenService;
	
    @Autowired
    UserRepository userRepository;
    
    @Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		var token = this.recoverToken(request);
        var login = tokenService.validateToken(token);
        
        if(login != null) {
        	User user = userRepository.findByEmail(login).orElseThrow(() -> new RuntimeException("User Not Found"));
        	var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        	var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        	SecurityContextHolder.getContext().setAuthentication(authentication);
        }	
        
        filterChain.doFilter(request, response);
	}

 
	private String recoverToken(HttpServletRequest request) {
		var autHeader = request.getHeader("Authorization");
		if(autHeader == null) {
			return null;
		}
		return autHeader.replace("Bearer ", "");
		
	}
}
