import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class FxmlTester {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                System.out.println("Testing Welcome.fxml");
                FXMLLoader.load(FxmlTester.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/Welcome.fxml"));
                System.out.println("Testing Login.fxml");
                FXMLLoader.load(FxmlTester.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/Login.fxml"));
                System.out.println("Testing AdminPanel.fxml");
                FXMLLoader.load(FxmlTester.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/AdminPanel.fxml"));
                System.out.println("Testing ChatView.fxml");
                FXMLLoader.load(FxmlTester.class.getResource("/com/rplbo/app/rpl_wedmateassistant/view/ChatView.fxml"));
                System.out.println("ALL OK");
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
