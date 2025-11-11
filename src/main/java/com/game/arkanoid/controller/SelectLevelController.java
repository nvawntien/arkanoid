package com.game.arkanoid.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller cho màn hình Chọn Level (SelecLevel.fxml).
 * Quản lý việc hiển thị ảnh/tên level và cho phép điều hướng Trái/Phải.
 */
public class SelectLevelController {

    @FXML
    private ImageView levelImageView;
    @FXML
    private Label levelNameLabel;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;

    /**
     * Tham chiếu đến SceneController chính để điều hướng
     * (ví dụ: quay về menu hoặc bắt đầu game).
     */
    private final SceneController sceneController;

    private List<String> levelImagePaths;
    private List<String> levelNames;
    private int currentLevelIndex = 0;

    /**
     * Hàm tạo, nhận SceneController để điều hướng.
     * @param sceneController instance của SceneController
     */
    public SelectLevelController(SceneController sceneController) {
        this.sceneController = Objects.requireNonNull(sceneController, "sceneController must not be null");
    }

    /**
     * Phương thức này được tự động gọi sau khi file FXML được load.
     */
    @FXML
    public void initialize() {
        // --- PHẦN ĐÃ CẬP NHẬT VỚI DỮ LIỆU CỦA BẠN ---
        // Khởi tạo danh sách ảnh và tên.
        // Đảm bảo các đường dẫn này chính xác và bắt đầu bằng "/"
        
        levelImagePaths = new ArrayList<>();
        levelImagePaths.add("/com/game/arkanoid/images/level1.png"); 
        levelImagePaths.add("/com/game/arkanoid/images/level2.png");
        levelImagePaths.add("/com/game/arkanoid/images/level3.png");
        levelImagePaths.add("/com/game/arkanoid/images/level4.png"); // <-- ĐÃ THÊM

        levelNames = new ArrayList<>();
        levelNames.add("Màn 1: Thuy Tinh"); // <-- ĐÃ CẬP NHẬT
        levelNames.add("Màn 2: Hoa Tinh"); // <-- ĐÃ CẬP NHẬT
        levelNames.add("Màn 3: Moc Tinh"); // <-- ĐÃ CẬP NHẬT
        levelNames.add("Màn 4: Tho Tinh"); // <-- ĐÃ THÊM
        // --- KẾT THÚC PHẦN CẬP NHẬT ---

        if (!levelImagePaths.isEmpty()) {
            updateLevelDisplay();
        } else {
            levelNameLabel.setText("Không tìm thấy level!");
            System.err.println("Lỗi: Danh sách levelImagePaths hoặc levelNames đang rỗng!");
        }
    }

    /**
     * Được gọi khi nhấn nút "Phải" (Next)
     */
    @FXML
    private void onNextButton() {
        if (levelImagePaths.isEmpty()) return;

        currentLevelIndex++;
        if (currentLevelIndex >= levelImagePaths.size()) {
            currentLevelIndex = 0; // Quay vòng
        }
        updateLevelDisplay();
    }

    /**
     * Được gọi khi nhấn nút "Trái" (Previous)
     */
    @FXML
    private void onPrevButton() {
        if (levelImagePaths.isEmpty()) return;

        currentLevelIndex--;
        if (currentLevelIndex < 0) {
            currentLevelIndex = levelImagePaths.size() - 1; // Quay vòng
        }
        updateLevelDisplay();
    }

    /**
     * Hàm trợ giúp: Cập nhật ImageView và Label dựa trên currentLevelIndex
     */
    private void updateLevelDisplay() {
        try {
            String imagePath = levelImagePaths.get(currentLevelIndex);
            // Dùng getResourceAsStream để load từ thư mục resources
            Image newImage = new Image(getClass().getResourceAsStream(imagePath));
            levelImageView.setImage(newImage);

            if (currentLevelIndex < levelNames.size()) {
                levelNameLabel.setText(levelNames.get(currentLevelIndex));
            } else {
                levelNameLabel.setText("Level " + (currentLevelIndex + 1));
            }

        } catch (Exception e) {
            System.err.println("Lỗi: Không thể load ảnh: " + (levelImagePaths.isEmpty() ? "N/A" : levelImagePaths.get(currentLevelIndex)));
            // e.printStackTrace(); // Bật lên để debug nếu cần
            levelNameLabel.setText("Lỗi load ảnh!");
            levelImageView.setImage(null);
        }
    }
    
    /**
     * (VÍ DỤ) Thêm hàm này để xử lý sự kiện cho nút "Bắt đầu" (nếu bạn thêm vào FXML)
     */
    @FXML
    private void onStartGame() {
        // Bắt đầu game với level đã chọn (index là 0, 1, 2... nên +1)
        int levelToStart = currentLevelIndex + 1;
        sceneController.showGameRound(levelToStart);
    }

    /**
     * (VÍ DỤ) Thêm hàm này để xử lý sự kiện cho nút "Quay lại" (nếu bạn thêm vào FXML)
     */
    @FXML
    private void onBackButton() {
        sceneController.showMenu();
    }
}