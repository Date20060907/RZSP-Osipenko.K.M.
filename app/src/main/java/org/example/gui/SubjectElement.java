package org.example.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.dao.SubjectDao;
import org.example.dao.SubjectGroupLinkDao;
import org.example.dataclasses.Subject;

import java.io.IOException;
import java.util.List;

/**
 * Ячейка списка предметов, отображающая название предмета и кнопку удаления.
 * Учитывает принадлежность к определённой группе и управляет связью "предмет–группа".
 */
public class SubjectElement extends ListCell<Subject> {

    private static final Logger logger = LogManager.getLogger(SubjectElement.class);

    /**
     * Метка с названием предмета.
     */
    @FXML
    private Label subjectNameLabel;

    /**
     * Кнопка удаления связи между предметом и группой.
     */
    @FXML
    private Button deleteButton;

    /**
     * Корневой элемент макета, загруженный из FXML.
     */
    private HBox root;

    /**
     * Идентификатор группы, с которой связана текущая дисциплина.
     */
    private final int groupId;

    /**
     * Создаёт новую ячейку для отображения предмета в контексте указанной группы.
     *
     * @param groupId идентификатор группы
     * @throws RuntimeException если не удаётся загрузить FXML-файл
     */
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

    /**
     * Обработчик нажатия кнопки удаления.
     * Удаляет связь между предметом и группой.
     * Если предмет больше нигде не используется, удаляет и сам предмет.
     */
    @FXML
    private void delete() {
        Subject itemToDelete = getItem();
        if (itemToDelete == null) {
            return;
        }

        getListView().getItems().remove(itemToDelete);

        SubjectGroupLinkDao linkDao = new SubjectGroupLinkDao();
        SubjectDao subjectDao = new SubjectDao();

        boolean linkDeleted = linkDao.deleteByGroupIdAndSubjectId(groupId, itemToDelete.getId());
        if (!linkDeleted) {
            logger.error("Не удалось удалить связь между группой {} и предметом \"{}\"", groupId, itemToDelete.getName());
            return;
        }

        List<Integer> remainingGroupIds = linkDao.findGroupIdsBySubjectId(itemToDelete.getId());
        if (remainingGroupIds.isEmpty()) {
            boolean subjectDeleted = subjectDao.deleteById(itemToDelete.getId());
            if (subjectDeleted) {
                logger.info("Предмет \"{}\" полностью удалён, так как не используется ни в одной группе", itemToDelete.getName());
            } else {
                logger.error("Не удалось удалить предмет \"{}\" из базы данных", itemToDelete.getName());
            }
        } else {
            logger.info("Связь с предметом \"{}\" для группы {} удалена. Предмет сохранён (используется в других группах)", 
                        itemToDelete.getName(), groupId);
        }
    }

    /**
     * Обновляет содержимое ячейки в зависимости от связанного объекта предмета.
     *
     * @param item объект предмета или null
     * @param empty флаг, указывающий, является ли ячейка пустой
     */
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