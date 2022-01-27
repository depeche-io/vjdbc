package de.simplicit.vjdbc.server.servlet;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Optional;

public class JettyEmbeddedServer {

    public static void main(String[] argv) throws Exception {

        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(
                Optional.of(Integer.parseInt(
                        System.getenv(EnvServletCommandSink.ENV_PREFIX + "PORT"))).orElse(8080));
        server.addConnector(connector);

        // Create a ServletContextHandler with contextPath.
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("");

        // Add the Servlet implementing the cart functionality to the context.
        ServletHolder servletHolder = context.addServlet(EnvServletCommandSink.class, "/*");
        servletHolder.setEnabled(true);
        servletHolder.start();

        // Link the context to the server.
        server.setHandler(context);

        server.start();
    }
}
