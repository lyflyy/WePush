package com.fangxuele.tool.push.ui.dialog;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.fangxuele.tool.push.App;
import com.fangxuele.tool.push.dao.TWxCpAppMapper;
import com.fangxuele.tool.push.domain.TWxCpApp;
import com.fangxuele.tool.push.ui.form.SettingForm;
import com.fangxuele.tool.push.util.ComponentUtil;
import com.fangxuele.tool.push.util.JTableUtil;
import com.fangxuele.tool.push.util.MybatisUtil;
import com.fangxuele.tool.push.util.SqliteUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * <pre>
 * WxCpAppDialog
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">Zhou Bo</a>
 * @since 2019/6/7.
 */
public class WxCpAppDialog extends JDialog {
    private static final long serialVersionUID = -1508175617622474963L;
    private JPanel contentPane;
    private JButton buttonDelete;
    private JButton buttonCancel;
    private JTable appsTable;
    private JTextField appNameTextField;
    private JTextField agentIdTextField;
    private JTextField secretTextField;
    private JButton saveButton;

    private Log logger = LogFactory.get();
    private static TWxCpAppMapper wxCpAppMapper = MybatisUtil.getSqlSession().getMapper(TWxCpAppMapper.class);

    public WxCpAppDialog() {
        super(App.mainFrame, "企业号/企业微信 应用管理");
        setContentPane(contentPane);
        setModal(true);

        ComponentUtil.setPreferSizeAndLocateToCenter(this, 0.5, 0.5);

        // 保存按钮事件
        saveButton.addActionListener(e -> {
            String appName = appNameTextField.getText();
            if (StringUtils.isBlank(appName)) {
                JOptionPane.showMessageDialog(this, "请填写账号名称！", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String agentId = agentIdTextField.getText();
            if (StringUtils.isBlank(agentId)) {
                JOptionPane.showMessageDialog(this, "请填写AgentId！", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            boolean update = false;
            List<TWxCpApp> tWxCpAppList = wxCpAppMapper.selectByAppName(appName);
            if (tWxCpAppList.size() > 0) {
                update = true;
            }

            TWxCpApp tWxCpApp = new TWxCpApp();
            String now = SqliteUtil.nowDateForSqlite();
            tWxCpApp.setAppName(appName);
            tWxCpApp.setCorpid(SettingForm.getInstance().getWxCpCorpIdTextField().getText());
            tWxCpApp.setAgentId(agentIdTextField.getText());
            tWxCpApp.setSecret(secretTextField.getText());
            tWxCpApp.setModifiedTime(now);

            if (update) {
                tWxCpApp.setId(tWxCpAppList.get(0).getId());
                wxCpAppMapper.updateByPrimaryKeySelective(tWxCpApp);
            } else {
                tWxCpApp.setCreateTime(now);
                wxCpAppMapper.insert(tWxCpApp);
            }
            renderTable();
            SettingForm.initSwitchMultiAccount();
            JOptionPane.showMessageDialog(this, "保存成功！", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // 删除按钮事件
        buttonDelete.addActionListener(e -> ThreadUtil.execute(() -> {
            try {
                int[] selectedRows = appsTable.getSelectedRows();
                if (selectedRows.length == 0) {
                    JOptionPane.showMessageDialog(this, "请至少选择一个！", "提示",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    int isDelete = JOptionPane.showConfirmDialog(this, "确认删除？", "确认",
                            JOptionPane.YES_NO_OPTION);
                    if (isDelete == JOptionPane.YES_OPTION) {
                        DefaultTableModel tableModel = (DefaultTableModel) appsTable.getModel();
                        for (int i = selectedRows.length; i > 0; i--) {
                            int selectedRow = appsTable.getSelectedRow();
                            Integer selectedId = (Integer) tableModel.getValueAt(selectedRow, 0);
                            wxCpAppMapper.deleteByPrimaryKey(selectedId);
                            tableModel.removeRow(selectedRow);
                        }
                        SettingForm.initSwitchMultiAccount();
                    }
                }
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, "删除失败！\n\n" + e1.getMessage(), "失败",
                        JOptionPane.ERROR_MESSAGE);
                logger.error(e1);
            }
        }));

        appsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clearFields();

                int selectedRow = appsTable.getSelectedRow();
                String selectedId = appsTable.getValueAt(selectedRow, 0).toString();
                TWxCpApp tWxCpApp = wxCpAppMapper.selectByPrimaryKey(Integer.valueOf(selectedId));
                appNameTextField.setText(tWxCpApp.getAppName());
                agentIdTextField.setText(tWxCpApp.getAgentId());
                secretTextField.setText(tWxCpApp.getSecret());
                super.mousePressed(e);
            }
        });

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    /**
     * 应用列表表格
     */
    public void renderTable() {
        String[] headerNames = {"id", "应用名称", "AgentId", "Secret"};
        DefaultTableModel model = new DefaultTableModel(null, headerNames);
        appsTable.setModel(model);

        DefaultTableCellRenderer hr = (DefaultTableCellRenderer) appsTable.getTableHeader()
                .getDefaultRenderer();
        // 表头列名居左
        hr.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);

        List<TWxCpApp> wxCpAppList = wxCpAppMapper.selectAll();
        Object[] data;
        for (TWxCpApp tWxCpApp : wxCpAppList) {
            data = new Object[4];
            data[0] = tWxCpApp.getId();
            data[1] = tWxCpApp.getAppName();
            data[2] = tWxCpApp.getAgentId();
            data[3] = tWxCpApp.getSecret();
            model.addRow(data);
        }

        // 隐藏id列
        JTableUtil.hideColumn(appsTable, 0);
    }

    /**
     * 清空表单
     */
    public void clearFields() {
        appNameTextField.setText("");
        agentIdTextField.setText("");
        secretTextField.setText("");
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonDelete = new JButton();
        buttonDelete.setText("删除");
        panel2.add(buttonDelete, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("好了");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(5, 5, 0, 5), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "添加应用"));
        final JLabel label1 = new JLabel();
        label1.setText("应用名称");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        appNameTextField = new JTextField();
        panel3.add(appNameTextField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("AgentId");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        agentIdTextField = new JTextField();
        panel3.add(agentIdTextField, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Secret");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        secretTextField = new JTextField();
        panel3.add(secretTextField, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        saveButton = new JButton();
        saveButton.setText("保存");
        panel3.add(saveButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        appsTable = new JTable();
        appsTable.setRowHeight(36);
        scrollPane1.setViewportView(appsTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
