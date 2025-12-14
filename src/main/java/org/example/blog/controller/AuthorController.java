package org.example.blog.controller;

public class AuthorController implements MainChildController {

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Никакой логики пока не нужно, информация статическая.
}
