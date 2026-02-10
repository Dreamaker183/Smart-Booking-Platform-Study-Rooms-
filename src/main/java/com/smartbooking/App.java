package com.smartbooking;

import com.smartbooking.service.AppBootstrap;
import com.smartbooking.service.AppServices;
import com.smartbooking.ui.ConsoleUI;
import com.smartbooking.ui.FxApp;
import javafx.application.Application;

public class App {
    public static void main(String[] args) {
        AppServices services = AppBootstrap.initialize();

        // Start WebServer in background for Web Client
        new Thread(() -> {
            try {
                com.smartbooking.web.WebServer.start(services, 8080);
            } catch (Exception e) {
                System.err.println("Failed to start WebServer: " + e.getMessage());
            }
        }).start();

        if (args.length > 0 && "cli".equalsIgnoreCase(args[0])) {
            ConsoleUI ui = new ConsoleUI(
                    services.getAuthService(),
                    services.getResourceService(),
                    services.getBookingService(),
                    services.getNotificationService(),
                    services.getAuditService());
            ui.start();
        } else {
            FxApp.setServices(services);
            Application.launch(FxApp.class, args);
        }
    }
}
