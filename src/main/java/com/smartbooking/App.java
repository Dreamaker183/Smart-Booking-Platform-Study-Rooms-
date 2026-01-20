package com.smartbooking;

import com.smartbooking.service.AppBootstrap;
import com.smartbooking.service.AppServices;
import com.smartbooking.ui.ConsoleUI;
import com.smartbooking.ui.FxApp;
import javafx.application.Application;

public class App {
    public static void main(String[] args) {
        AppServices services = AppBootstrap.initialize();
        if (args.length > 0 && "cli".equalsIgnoreCase(args[0])) {
            ConsoleUI ui = new ConsoleUI(
                    services.getAuthService(),
                    services.getResourceService(),
                    services.getBookingService(),
                    services.getNotificationService(),
                    services.getAuditService()
            );
            ui.start();
        } else {
            FxApp.setServices(services);
            Application.launch(FxApp.class, args);
        }
    }
}
