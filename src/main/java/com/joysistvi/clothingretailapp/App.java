package com.joysistvi.clothingretailapp;

import com.joysistvi.clothingretailapp.Controller.Product_Variant;
import com.joysistvi.clothingretailapp.Repository.Product_VariantRepository;
import com.joysistvi.clothingretailapp.Repository.Product_VariantRepositoryImpl;
import com.joysistvi.clothingretailapp.Repository.UserRepository;
import com.joysistvi.clothingretailapp.Repository.UserRepositoryImpl;
import com.joysistvi.clothingretailapp.Service.Product_VariantService;
import com.joysistvi.clothingretailapp.Service.Product_VariantServiceImpl;
import com.joysistvi.clothingretailapp.Service.UserService;
import com.joysistvi.clothingretailapp.Service.UserServiceImpl;
import com.joysistvi.clothingretailapp.View.Product_VariantView;
import com.joysistvi.clothingretailapp.View.UserView;
import com.joysistvi.clothingretailapp.config.DbConnection;
import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class App {
    private final DbConnection dbConnection = new DbConnection();
    private final Scanner scanner = new Scanner(System.in);

    private final Product_VariantRepository productVariantRepository = new Product_VariantRepositoryImpl(dbConnection);
    private final Product_VariantService productVariantService = new Product_VariantServiceImpl(productVariantRepository);
    private final Product_VariantView productVariantView = new Product_VariantView(productVariantService, scanner);

    private final UserRepository userRepository = new UserRepositoryImpl(dbConnection);
    private final UserService userService = new UserServiceImpl(userRepository);
    private final UserView userView = new UserView(userService, scanner);


    public static void main(String[] args) {
        App app = new App();

        // Product Variants MANUAL TESTING
        // app.productVariantView.handleMenu(); // passed

        // Product Variants MANUAL TESTING
        //app.userView.displayAllUsers(); // passed
        //app.userView.promptCreateUser(); // passed
        //app.userView.promptUpdateUser(); // passed
        //app.userView.promptDeleteUser(); // passed
        //app.userView.promptArchiveUser(); // passed
        //app.userView.promptRestoreUser(); // passed
        //app.userView.promptSearchUsers(); // passed
        //app.userView.promptLoginUser(); // passed

    }
}