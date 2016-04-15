package gr.grnet.dep.server.servlet.filter;

import gr.grnet.dep.service.util.DEPConfigurationFactory;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter("/*")
public class MemoryLogger implements Filter {

	@Inject
	Logger logger;

	private static final int MIN_MEMORY = 512 * 1024 * 1024; // 512MB

	private boolean isEnabled() {
		boolean enabled = false;
		try {
			enabled = DEPConfigurationFactory.getServerConfiguration().getBoolean("debug.memory.enabled", false);
		} catch (Exception e) {
		}
		return enabled;
	}

	@Override public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		// 1. Check if enabled
		if (!isEnabled()) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}
		if (Runtime.getRuntime().freeMemory() > MIN_MEMORY) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		long start = System.currentTimeMillis();
		logger.info("StartCurrentTotalFreeMaxPath;" +
				start + ";" +
				System.currentTimeMillis() + ";" +
				Runtime.getRuntime().totalMemory() + ";" +
				Runtime.getRuntime().freeMemory() + ";" +
				Runtime.getRuntime().maxMemory() + ";" +
				"START;" +
				((HttpServletRequest) servletRequest).getPathInfo()
		);

		filterChain.doFilter(servletRequest, servletResponse);

		logger.info("StartCurrentTotalFreeMaxPath;" +
				start + ";" +
				System.currentTimeMillis() + ";" +
				Runtime.getRuntime().totalMemory() + ";" +
				Runtime.getRuntime().freeMemory() + ";" +
				Runtime.getRuntime().maxMemory() + ";" +
				"END;" +
				((HttpServletRequest) servletRequest).getPathInfo()
		);
	}

	@Override public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override public void destroy() {
	}
}
