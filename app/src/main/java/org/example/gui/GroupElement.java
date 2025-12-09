package org.example.gui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dao.GroupDao;
import org.example.dataclasses.Group;

import java.io.IOException;

/**
 * Ячейка списка групп, отображающая название группы и кнопку удаления.
 * Используется в {@link ListView} для отображения списка групп.
 */
public class GroupElement extends ListCell<Group> {

    private static final Logger logger = LogManager.getLogger(GroupElement.class);

    /**
     * Корневой контейнер, загруженный из FXML.
     */
    private final HBox root;

    /**
     * Метка с названием группы.
     */
    private final Label groupNameLabel;

    /**
     * Кнопка удаления группы.
     */
    private final Button deleteButton;

    /**
     * Создаёт новую ячейку, загружая её внешний вид из файла FXML.
     * Инициализирует обработчик события для кнопки удаления.
     *
     * @throws RuntimeException если не удаётся загрузить FXML-файл
     */
    public GroupElement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GroupElement.fxml"));
            loader.setController(this);
            root = loader.load();

            groupNameLabel = (Label) root.lookup("#groupNameLabel");
            deleteButton = (Button) root.lookup("#deleteButton");

            deleteButton.setOnAction(e -> delete());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить GroupElement.fxml", e);
        }
    }

    /**
     * Обработчик нажатия кнопки удаления.
     * Удаляет группу из списка и из базы данных.
     */
    @FXML
    private void delete() {
        Group itemToDelete = getItem();
        if (itemToDelete != null) {
            ListView<Group> listView = getListView();
            if (listView != null) {
                ObservableList<Group> items = listView.getItems();
                items.remove(itemToDelete);

                GroupDao groupDao = new GroupDao();
                boolean deleted = groupDao.deleteById(itemToDelete.getId());
                if (deleted) {
                    logger.info("Группа \"{}\" успешно удалена", itemToDelete.getName());
                } else {
                    logger.warn("Не удалось удалить группу \"{}\" из базы данных", itemToDelete.getName());
                }
            }
        }
    }

    /**
     * Обновляет содержимое ячейки в зависимости от связанного объекта группы.
     *
     * @param item объект группы или null
     * @param empty флаг, указывающий, является ли ячейка пустой
     */
    @Override
    protected void updateItem(Group item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            groupNameLabel.setText(item.getName());
            setGraphic(root);
        }
    }
}