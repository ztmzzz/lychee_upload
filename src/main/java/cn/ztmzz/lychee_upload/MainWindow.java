package cn.ztmzz.lychee_upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cn.ztmzz.lychee_upload.UploadUtil.*;


public class MainWindow {
    public static String domain, username, password;
    public static int upload_num = 0;

    public static void main(String[] args) {
        try {
            //配置读取
//            String config = new String(Files.readAllBytes(Paths.get("config.json")));
            String config = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(Objects.requireNonNull(MainWindow.class.getClassLoader().getResourceAsStream("config.json"))));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            config = sb.toString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(config);
            domain = node.get("domain").asText();
            username = node.get("username").asText();
            password = node.get("password").asText();
            //窗口
            JFrame frame = new JFrame("快捷上传图床");
            frame.setSize(450, 300);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);//窗体居中显示
            JPanel panel = new JPanel();
            frame.add(panel);
            placeComponents(panel);
            frame.setVisible(true);
            init_server(panel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void init_server(JPanel panel) {
        //获取message窗口
        Component[] myComps = panel.getComponents();
        JTextField message = null;
        for (Component myComp : myComps) {
            if (myComp instanceof JTextField temp) {
                if (temp.getText().equals("初始化中")) {
                    message = temp;
                }
            }
        }
        try {
            assert message != null;
            set_domain(domain);
            message.setText("连接服务器中");
            init();
            message.setText("登录中");
            login(username, password);
            message.setText("登录成功");
        } catch (Exception e) {
            e.printStackTrace();
            message.setText("初始化失败");
        }
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);
        int label_x = 10, label_y = 20;
        int label_width = 80, label_height = 30;
        int text_x = 100, text_y = 20;
        int text_width = 300, text_height = 30;
        int button_x = 10, button_y;
        int button_width = 180, button_height = 70;
        int gap = 40;

        // 输入图片地址
        JLabel pic_input_label = new JLabel("图片输入地址:");
        pic_input_label.setBounds(label_x, label_y, label_width, label_height);
        label_y += gap;
        panel.add(pic_input_label);
        final JTextField pic_input = new JTextField();
        pic_input.setBounds(text_x, text_y, text_width, text_height);
        text_y += gap;
        panel.add(pic_input);

        // 输出图片的地址
        JLabel pic_output_label = new JLabel("图片地址:");
        pic_output_label.setBounds(label_x, label_y, label_width, label_height);
        label_y += gap;
        panel.add(pic_output_label);
        final JTextField pic_output = new JTextField();
        pic_output.setBounds(text_x, text_y, text_width, text_height);
        text_y += gap;
        panel.add(pic_output);

        // 上传相册
        JLabel upload_album_label = new JLabel("上传相册:");
        upload_album_label.setBounds(label_x, label_y, label_width, label_height);
        label_y += gap;
        panel.add(upload_album_label);
        final JTextField upload_album = new JTextField("默认图床");
        upload_album.setBounds(text_x, text_y, text_width, text_height);
        text_y += gap;
        panel.add(upload_album);

        // 运行消息
        JLabel message_label = new JLabel("程序信息:");
        message_label.setBounds(label_x, label_y, label_width, label_height);
        label_y += gap;
        panel.add(message_label);
        final JTextField message = new JTextField("初始化中");
        message.setBounds(text_x, text_y, text_width, text_height);
        text_y += gap;
        panel.add(message);

        button_y = text_y;

        // 创建上传按钮
        JButton upload_button = new JButton("上传");
        upload_button.setBounds(button_x, button_y, button_width, button_height);
        upload_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    message.setText("");
                    pic_output.setText("");
                    String res = upload(pic_input.getText(), upload_album.getText());
                    check_upload_res(res, message, pic_output);
                } catch (Exception e1) {
                    message.setText(e1.getMessage());
                }
            }
        });
        panel.add(upload_button);

        //创建从剪贴板上传按钮
        JButton upload_from_clipboard_button = new JButton("从剪贴板上传");
        upload_from_clipboard_button.setBounds(button_x + button_width + gap, button_y, button_width, button_height);
        upload_from_clipboard_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                message.setText("");
                pic_output.setText("");
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable tf = c.getContents(null);
                String[] img_last_name = new String[]{"jpg", "png", "gif", "jpeg", "bmp", "tiff", "raw", "webp", "svg"};
                try {
                    if (tf.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                        Object o = tf.getTransferData(DataFlavor.javaFileListFlavor);
                        List<File> files = castList(o, File.class);
                        String file_url = files.get(0).toString();
                        //判断是否为图片
                        int dot = file_url.lastIndexOf('.');
                        String last_name = file_url.substring(dot + 1);
                        if (Arrays.asList(img_last_name).contains(last_name)) {
                            pic_input.setText(file_url);
                            String res = upload(pic_input.getText(), upload_album.getText());
                            check_upload_res(res, message, pic_output);
                        } else {
                            message.setText("不是图片");
                        }

                    } else if (tf.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                        Image upload_img = (Image) tf.getTransferData(DataFlavor.imageFlavor);
                        pic_input.setText("剪贴板文件");
                        String res = upload(upload_img, upload_album.getText());
                        check_upload_res(res, message, pic_output);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    message.setText(ex.getMessage());
                }
            }
        });
        panel.add(upload_from_clipboard_button);

        //处理拖拽文件
        TransferHandler th = new TransferHandler() {
            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    Object o = t.getTransferData(DataFlavor.javaFileListFlavor);

                    String filepath = o.toString();
                    if (filepath.startsWith("[")) {
                        filepath = filepath.substring(1);
                    }
                    if (filepath.endsWith("]")) {
                        filepath = filepath.substring(0, filepath.length() - 1);
                    }
//                    System.out.println(filepath);
                    pic_input.setText(filepath);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                for (DataFlavor flavor : flavors) {
                    if (DataFlavor.javaFileListFlavor.equals(flavor)) {
                        return true;
                    }
                }
                return false;
            }
        };
        panel.setTransferHandler(th);
        Component[] myComps = panel.getComponents();
        for (Component myComp : myComps) {
            if (myComp instanceof JComponent temp) {
                temp.setTransferHandler(th);
            }
        }
    }

    private static void check_upload_res(String res, JTextField message, JTextField pic_output) {
        if (res.equals("false")) {
            message.setText("上传失败");
        } else {
            message.setText("上传第" + (++upload_num) + "张图片成功");
            pic_output.setText(res);
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable tf = new StringSelection(res);
            c.setContents(tf, null);
        }
    }

    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }
}
