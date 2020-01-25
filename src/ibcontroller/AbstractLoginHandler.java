// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2016 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBController is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBController is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBController.  If not, see <http://www.gnu.org/licenses/>.

package ibcontroller;

import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.*;

public abstract class AbstractLoginHandler implements WindowHandler {
    
    @Override
    public boolean filterEvent(Window window, int eventId) {
        switch (eventId) {
            case WindowEvent.WINDOW_OPENED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public final void handleWindow(Window window, int eventID) {
        LoginManager.loginManager().setLoginFrame((JFrame) window);

        try {
            if (!initialise(window, eventID)) return;
            if (!setFields(window, eventID)) return;
            if (!preLogin(window, eventID)) return;
            doLogin(window);
        } catch (IBControllerException e) {
            Utils.logError("could not login: could not find control: " + e.getMessage());
            System.exit(1);
        }
    }
    
    @Override
    public abstract boolean recogniseWindow(Window window);
    
    private void doLogin(final Window window) throws IBControllerException {
        if (SwingUtils.findButton(window, "Login") == null) throw new IBControllerException("Login button");

        GuiDeferredExecutor.instance().execute(new Runnable() {
            @Override
            public void run() {
                SwingUtils.clickButton(window, "Login");
            }
        });
    }
    
    protected abstract boolean initialise(final Window window, int eventID) throws IBControllerException;
    
    protected abstract boolean preLogin(final Window window, int eventID) throws IBControllerException;
    
    protected abstract boolean setFields(Window window, int eventID) throws IBControllerException;

    protected final void setMissingCredential(final Window window, final int credentialIndex) {
        SwingUtils.findTextField(window, credentialIndex).requestFocus();
    }

    protected final void setCredential(final Window window, 
                                            final String credentialName,
                                            final int credentialIndex, 
                                            final String value) throws IBControllerException {
        if (! SwingUtils.setTextField(window, credentialIndex, value)) throw new IBControllerException(credentialName);
    }

    private void switchToPaperTrading(Window window) throws IBControllerException {
        JRadioButton button = SwingUtils.findRadioButton(window, "Paper Trading");
        if (button == null) throw new IBControllerException("Paper Trading button");
        if (! button.isSelected()) button.doClick();
    }

    private void switchToLiveTrading(Window window) throws IBControllerException {
        JRadioButton button = SwingUtils.findRadioButton(window, "Live Trading");
        if (button == null) throw new IBControllerException("Live Trading button");
        if (! button.isSelected()) button.doClick();
    }

    protected final void setTradingModeCombo978(final Window window) throws IBControllerException {
        Utils.logToConsole("setTradingModeCombo");
        String tradingMode = TradingModeManager.tradingModeManager().getTradingMode();
        Utils.logToConsole("Setting Trading mode = " + tradingMode);
        if (tradingMode.equalsIgnoreCase(TradingModeManager.TRADING_MODE_LIVE)) {
            switchToLiveTrading(window);
        } else {
            switchToPaperTrading(window);
        }
    }

    protected final void setTradingModeCombo(final Window window) throws IBControllerException {
        int version = Settings.settings().getInt("tws_major_version", 972);
        Utils.logToConsole("setTradingModeCombo for tws version: " + version);
        if(version >= 978) {
            setTradingModeCombo978(window);
        } else{
            if (SwingUtils.findLabel(window, "Trading Mode") != null) {
                JComboBox<?> tradingModeCombo;
                if (Settings.settings().getBoolean("FIX", false)) {
                    tradingModeCombo = SwingUtils.findComboBox(window, 1);
                } else {
                    tradingModeCombo = SwingUtils.findComboBox(window, 0);
                }

                if (tradingModeCombo != null) {
                    String tradingMode = TradingModeManager.tradingModeManager().getTradingMode();
                    Utils.logToConsole("Setting Trading mode = " + tradingMode);
                    if (tradingMode.equalsIgnoreCase(TradingModeManager.TRADING_MODE_LIVE)) {
                        tradingModeCombo.setSelectedItem("Live Trading");
                    } else {
                        tradingModeCombo.setSelectedItem("Paper Trading");
                    }
                }
            }
        }
    }
}
