package org.example.gui;

import java.io.IOException;

import org.example.dao.GroupDao;
import org.example.dao.SubjectDao;
import org.example.dataclasses.Subject;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

public class SubjectElement extends ListCell<Subject> {

	private final HBox root;
	private final Label subjectNameLabel;
	private final Button deleteButton;

	public SubjectElement() {
		try {
			// Загружаем FXML один раз при создании ячейки
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/SubjectElement.fxml"));
			loader.setController(this);
			root = loader.load();

			subjectNameLabel = (Label) root.lookup("#subjectNameLabel");
			deleteButton = (Button) root.lookup("#deleteButton");

			deleteButton.setOnAction(e -> delete());

		} catch (IOException e) {
			throw new RuntimeException("Не удалось загрузить SubjectElement.fxml", e);
		}
	}

@FXML
private void delete() {
    Subject itemToDelete = getItem();
    if (itemToDelete != null) {
        ListView<Subject> listView = getListView();
        if (listView != null) {
            // Удаляем из списка в интерфейсе
            listView.getItems().remove(itemToDelete);

            // Удаляем из базы данных
            SubjectDao subjectDao = new SubjectDao();
            subjectDao.deleteById(itemToDelete.getId());

            System.out.println("Дисциплина \"" + itemToDelete.getName() + "\" удалена.");
        }
    }
}

	@Override
	protected void updateItem(Subject item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null) {
			setGraphic(null);
		} else {
			subjectNameLabel.setText(item.getName());
			setGraphic(root);
		}
	}

}