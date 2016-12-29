package com.mercury.platform.ui;

import com.mercury.platform.shared.events.EventRouter;
import com.mercury.platform.shared.events.SCEvent;
import com.mercury.platform.shared.events.SCEventHandler;
import com.mercury.platform.shared.events.custom.*;
import com.mercury.platform.shared.pojo.Message;
import com.mercury.platform.ui.components.panel.MessagePanel;
import com.mercury.platform.ui.components.panel.MessagePanelStyle;
import com.mercury.platform.ui.misc.AppThemeColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Created by Константин on 24.12.2016.
 */
public class MessageFrame extends OverlaidFrame {
    private TradeMode tradeMode = TradeMode.DEFAULT;

    public MessageFrame(){
        super("Messages");
    }

    @Override
    protected void init() {
        super.init();
        setVisible(false);
        disableHideEffect(); // todo
    }

    public void convertFrameTo(TradeMode mode){
        this.tradeMode = mode;
    }

    @Override
    protected LayoutManager getFrameLayout() {
        return new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS);
    }

    @Override
    public void initHandlers() {
        EventRouter.registerHandler(ChangedTradeModeEvent.ToSuperTradeModeEvent.class, event -> {
            convertFrameTo(TradeMode.SUPER);
        });
        EventRouter.registerHandler(ChangedTradeModeEvent.ToDefaultTradeModeEvent.class, event -> {
            convertFrameTo(TradeMode.DEFAULT);
        });
        EventRouter.registerHandler(NewWhispersEvent.class, event -> {
            List<Message> messages = ((NewWhispersEvent) event).getMessages();
            for (Message message : messages) {
                MessagePanel messagePanel = null;
                switch (tradeMode){
                    case SUPER:{
                        messagePanel = new MessagePanel(message.getWhisperNickname(), message.getMessage(), MessagePanelStyle.BIGGEST);
                        break;
                    }
                    case DEFAULT:{
                        messagePanel = new MessagePanel(message.getWhisperNickname(), message.getMessage(), MessagePanelStyle.BIGGEST);
                        if(this.getContentPane().getComponentCount() > 0){
                            messagePanel.setStyle(MessagePanelStyle.SMALL);
                        }else {
                            messagePanel.setAsTopMessage();
                        }
                        messagePanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                MessagePanel source = (MessagePanel) e.getSource();
                                switch (source.getStyle()){
                                    case SMALL:{
                                        source.setStyle(MessagePanelStyle.BIGGEST);
                                        break;
                                    }
                                    case BIGGEST:{
                                        source.setStyle(MessagePanelStyle.SMALL);
                                        break;
                                    }
                                }
                                if(MessageFrame.this.getContentPane().getComponent(0).equals(source)){
                                    source.setAsTopMessage();
                                    source.setBorder(null);
                                    MessageFrame.this.repaint();
                                }
                                MessageFrame.this.pack();
                            }
                        });
                    }
                }
                if(this.getContentPane().getComponentCount() > 0){
                    messagePanel.setBorder(BorderFactory.createMatteBorder(1,0,0,0, AppThemeColor.BORDER));
                }
                this.add(messagePanel);
            }
            this.pack();
        });
        EventRouter.registerHandler(CloseMessagePanelEvent.class, event -> {
            this.remove(((CloseMessagePanelEvent) event).getComponent());
            this.pack();
        });
        EventRouter.registerHandler(DraggedMessageFrameEvent.class, event -> {
            int x = ((DraggedMessageFrameEvent) event).getX();
            int y = ((DraggedMessageFrameEvent) event).getY();
            MessageFrame.this.setLocation(x,y);
            configManager.saveComponentLocation(this.getClass().getSimpleName(),this.getLocation());
        });
        EventRouter.registerHandler(RepaintEvent.RepaintMessagePanel.class, event -> {
            MessageFrame.this.revalidate();
            MessageFrame.this.repaint();
        });
    }
    private enum TradeMode{
        DEFAULT,SUPER
    }
}