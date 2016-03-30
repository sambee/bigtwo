/*
Big Two for Game Gardens - a Chinese climbing-style card game
Copyright (C) 2005 Chris Nokleberg

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.
*/

package com.sixlegs.bigtwo.client;

import com.samskivert.swing.Controller;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.MultiLineLabel;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;
import com.sixlegs.bigtwo.data.BigTwoCodes;
import com.threerings.crowd.client.PlacePanel;
import com.threerings.parlor.turn.client.TurnDisplay;
import com.threerings.toybox.client.ChatPanel;
import com.threerings.toybox.client.ToyBoxUI;
import com.threerings.toybox.util.ToyBoxContext;
import com.threerings.util.MessageBundle;
import java.awt.*;
import javax.swing.*;
import static com.sixlegs.bigtwo.data.BigTwoCodes.*;

/**
 * Contains the primary client interface for the game.
 */
public class BigTwoPanel extends PlacePanel
{
    public BigTwoBoardView board;

    /**
     * Creates a BigTwo panel and its associated interface components.
     */
    public BigTwoPanel (ToyBoxContext ctx, BigTwoController ctrl)
    {
        super(ctrl);
        MessageBundle msgs = ctx.getMessageManager().getBundle(BIGTWO_MSG_BUNDLE);

        // give ourselves a wee bit of a border
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // HGroupLayout gl = new HGroupLayout(HGroupLayout.STRETCH);
        HGroupLayout gl = new HGroupLayout(HGroupLayout.STRETCH, 10, HGroupLayout.CENTER);
        gl.setOffAxisPolicy(HGroupLayout.STRETCH);
        setLayout(gl);

        // create the board
        add(board = new BigTwoBoardView(ctx));

        // create our side panel
        VGroupLayout sgl = new VGroupLayout(VGroupLayout.STRETCH);
        sgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        sgl.setJustification(VGroupLayout.TOP);
        JPanel sidePanel = new JPanel(sgl);

        // add a big fat label
        MultiLineLabel vlabel = new MultiLineLabel(msgs.get("m.title"));
        vlabel.setAntiAliased(true);
        vlabel.setFont(ToyBoxUI.fancyFont);
        sidePanel.add(vlabel, VGroupLayout.FIXED);

        // add a "back" button
        sidePanel.add(createButton(msgs, "m.back_to_lobby", BACK_TO_LOBBY),
                      VGroupLayout.FIXED);

        Icon icon = new ImageIcon(ctx.loadImage("media/tinyherb.png"));
        TurnDisplay turnDisplay = new TurnDisplay(icon);
        turnDisplay.setWinnerText(msgs.get("m.winner"));
        sidePanel.add(new JLabel(msgs.get("m.turn")), VGroupLayout.FIXED);
        sidePanel.add(turnDisplay, VGroupLayout.FIXED);

        JCheckBox suggest = new JCheckBox(msgs.get("m.suggest_a_hand"));
        suggest.setActionCommand(SUGGEST);
        suggest.addActionListener(Controller.DISPATCHER);
        suggest.setToolTipText(msgs.get("m.tooltip_suggest_a_hand"));
        sidePanel.add(suggest, VGroupLayout.FIXED);

        JCheckBox autoPass = new JCheckBox(msgs.get("m.auto_pass"));
        autoPass.setActionCommand(AUTO_PASS);
        autoPass.addActionListener(Controller.DISPATCHER);
        autoPass.setToolTipText(msgs.get("m.tooltip_auto_pass"));
        sidePanel.add(autoPass, VGroupLayout.FIXED);
        
        SwingUtil.setOpaque(sidePanel, false);
        setOpaque(true);
        setBackground(new Color(0xDAEB9C));

        // add a chat box
        ChatPanel chat = new ChatPanel(ctx);
        chat.removeSendButton();
        sidePanel.add(chat, 1);

        // add our side panel to the main display
        add(sidePanel, HGroupLayout.FIXED);
    }

    private static JButton createButton(MessageBundle msgs, String key, String command)
    {
        JButton button = new JButton(msgs.get(key));
        button.setActionCommand(command);
        button.addActionListener(Controller.DISPATCHER);
        return button;
    }
}
