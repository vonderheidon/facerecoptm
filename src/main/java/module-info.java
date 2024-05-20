module br.com.catolicapb.facerecoptm {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires libtensorflow;


    opens br.com.catolicapb.facerecoptm to javafx.fxml;
    opens br.com.catolicapb.facerecoptm.controller to javafx.fxml;
    exports br.com.catolicapb.facerecoptm;
    exports br.com.catolicapb.facerecoptm.controller;
}