package org.example.gui;

import java.io.IOException;

import org.example.dao.GroupDao;
import org.example.dataclasses.Group;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

public class GroupElement extends ListCell<Group> {

	private final HBox root;
	private final Label groupNameLabel;
	private final Button deleteButton;

	public GroupElement() {
		try {
			// Загружаем FXML один раз при создании ячейки
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/GroupElement.fxml"));
			loader.setController(this);
			root = loader.load();

			groupNameLabel = (Label) root.lookup("#groupNameLabel");
			deleteButton = (Button) root.lookup("#deleteButton");

			deleteButton.setOnAction(e -> delete());

		} catch (IOException e) {
			throw new RuntimeException("Не удалось загрузить GroupElement.fxml", e);
		}
	}

	@FXML
	private void delete() {
		        Group itemToDelete = getItem(); // Получаем Group, связанный с этой ячейкой
        if (itemToDelete != null) {
            // Получаем ссылку на ListView через getParent (HBox -> ListCell -> ListView)
            // Более надежный способ - через getListView()
            ListView<Group> listView = getListView();
            if (listView != null) {
                ObservableList<Group> items = listView.getItems();
                // Удаляем из модели представления
                items.remove(itemToDelete);

                // Удаляем из базы данных
                GroupDao groupDao = new GroupDao(); // Убедитесь, что GroupDao может быть создан или внедрен
                groupDao.deleteById(itemToDelete.getId()); // Предполагается, что Group имеет ID

                System.out.println("Группа \"" + itemToDelete.getName() + "\" удалена.");
            }
        }
	}

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