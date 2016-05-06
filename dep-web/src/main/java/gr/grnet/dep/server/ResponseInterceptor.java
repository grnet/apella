package gr.grnet.dep.server;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * This provider has been created in order to handle Broke Pipes IO Exceptions and
 * not to be logged
 *
 */

@Provider
public class ResponseInterceptor implements WriterInterceptor {

    private static Logger log = Logger.getLogger(ResponseInterceptor.class.getName());

    @Override
    public void aroundWriteTo(WriterInterceptorContext writerInterceptorContext) throws IOException, WebApplicationException {
        try {
            writerInterceptorContext.proceed();
        } catch (IOException e) {
            if (StringUtils.equals(e.getMessage(), "Broken pipe")) {
                log.fine("IOException: Broken Pipe");
            }
        }
    }
}
