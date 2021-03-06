package com.fangxuele.tool.push.ui.frame;

import com.apple.eawt.Application;
import com.fangxuele.tool.push.ui.UiConsts;
import com.fangxuele.tool.push.ui.listener.AboutListener;
import com.fangxuele.tool.push.ui.listener.BoostListener;
import com.fangxuele.tool.push.ui.listener.FrameListener;
import com.fangxuele.tool.push.ui.listener.HelpListener;
import com.fangxuele.tool.push.ui.listener.MemberListener;
import com.fangxuele.tool.push.ui.listener.MessageTypeListener;
import com.fangxuele.tool.push.ui.listener.MsgEditListener;
import com.fangxuele.tool.push.ui.listener.MsgManageListener;
import com.fangxuele.tool.push.ui.listener.PushHisListener;
import com.fangxuele.tool.push.ui.listener.PushListener;
import com.fangxuele.tool.push.ui.listener.ScheduleListener;
import com.fangxuele.tool.push.ui.listener.SettingListener;
import com.fangxuele.tool.push.ui.listener.TabListener;
import com.fangxuele.tool.push.util.ComponentUtil;
import com.fangxuele.tool.push.util.SystemUtil;
import org.apache.commons.compress.utils.Lists;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * <pre>
 * 主窗口
 * </pre>
 *
 * @author <a href="https://github.com/rememberber">Zhou Bo</a>
 * @since 2019/2/14.
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = -332963894416012132L;

    public void init() {
        this.setName(UiConsts.APP_NAME);
        this.setTitle(UiConsts.APP_NAME);
        List<Image> images = Lists.newArrayList();
        images.add(UiConsts.IMAGE_LOGO_1024);
        images.add(UiConsts.IMAGE_LOGO_512);
        images.add(UiConsts.IMAGE_LOGO_256);
        images.add(UiConsts.IMAGE_LOGO_128);
        images.add(UiConsts.IMAGE_LOGO_64);
        images.add(UiConsts.IMAGE_LOGO_48);
        images.add(UiConsts.IMAGE_LOGO_32);
        images.add(UiConsts.IMAGE_LOGO_24);
        images.add(UiConsts.IMAGE_LOGO_16);
        this.setIconImages(images);
        // Mac系统Dock图标
        if (SystemUtil.isMacOs()) {
            Application application = Application.getApplication();
            application.setDockIconImage(UiConsts.IMAGE_LOGO_1024);
            application.setEnabledAboutMenu(false);
            application.setEnabledPreferencesMenu(false);
        }

        ComponentUtil.setPreferSizeAndLocateToCenter(this, 0.8, 0.88);
    }

    /**
     * 添加事件监听
     */
    public void addListeners() {
        MessageTypeListener.addListeners();
        AboutListener.addListeners();
        HelpListener.addListeners();
        PushHisListener.addListeners();
        SettingListener.addListeners();
        MsgEditListener.addListeners();
        MsgManageListener.addListeners();
        MemberListener.addListeners();
        PushListener.addListeners();
        BoostListener.addListeners();
        ScheduleListener.addListeners();
        TabListener.addListeners();
        FrameListener.addListeners();
    }
}
