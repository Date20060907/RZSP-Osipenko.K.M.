package org.example.gui;

import java.io.IOException;
import java.util.List;

import org.example.dao.SubjectDao;
import org.example.dao.SubjectGroupLinkDao;
import org.example.dataclasses.Subject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

public class SubjectElement extends ListCell<Subject> {

	@FXML
	private Label subjectNameLabel;

	@FXML
	private Button deleteButton;

	private HBox root;
	private int groupId; // ← ID группы, с которой связана дисциплина

	// Конструктор с groupId
	public SubjectElement(int groupId) {
		this.groupId = groupId;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/SubjectElement.fxml"));
			loader.setController(this);
			root = loader.load();
		} catch (IOException e) {
			throw new RuntimeException("Не удалось загрузить SubjectElement.fxml", e);
		}
	}

	@FXML
	private void delete() {
		Subject itemToDelete = getItem();
		if (itemToDelete == null)
			return;

		// Удаляем из UI
		getListView().getItems().remove(itemToDelete);

		SubjectGroupLinkDao linkDao = new SubjectGroupLinkDao();
		SubjectDao subjectDao = new SubjectDao();

		// 1. Удаляем связь
		boolean linkDeleted = linkDao.deleteByGroupIdAndSubjectId(groupId, itemToDelete.getId());
		if (!linkDeleted) {
			System.err.println("Не удалось удалить связь для предмета: " + itemToDelete.getName());
			return;
		}

		// 2. Проверяем, есть ли ещё связи у этого предмета
		List<Integer> remainingGroupIds = linkDao.findGroupIdsBySubjectId(itemToDelete.getId());
		if (remainingGroupIds.isEmpty()) {
			// 3. Если связей нет — удаляем сам предмет
			boolean subjectDeleted = subjectDao.deleteById(itemToDelete.getId());
			if (subjectDeleted) {
				System.out.println("Предмет \"" + itemToDelete.getName() + "\" полностью удалён (не используется).");
			} else {
				System.err.println("Не удалось удалить предмет: " + itemToDelete.getName());
			}
		} else {
			System.out.println("Связь с дисциплиной \"" + itemToDelete.getName()
					+ "\" удалена. Предмет остаётся (используется в других группах).");
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