package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главный класс приложения, отвечающий за запуск JavaFX-интерфейса.
 * Инициализирует и отображает главное окно со списком групп.
 */
public class App extends Application {

    private static final Logger logger = LogManager.getLogger(App.class);

    /**
     * Запускает главное окно приложения, загружая FXML-файл GroupList.fxml
     * и отображая его в сцене размером 600x800 пикселей.
     *
     * @param primaryStage основное окно приложения
     * @throws Exception если не удаётся загрузить FXML-ресурс
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Запуск главного окна приложения");

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/GroupList.fxml")
        );

        Scene scene = new Scene(loader.load(), 600, 800);
        primaryStage.setTitle("Список групп");
        primaryStage.setScene(scene);
        primaryStage.show();

        logger.info("Главное окно успешно отображено");
    }

    /**
     * Точка входа в приложение. Передаёт управление JavaFX-фреймворку.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        logger.info("Запуск JavaFX-приложения");
        launch(args);
    }
}