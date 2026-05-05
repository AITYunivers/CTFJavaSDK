//----------------------------------------------------------------------------------
//
// CRUNSTATICTEXT: extension object
//
//----------------------------------------------------------------------------------

import Services.CFontInfo;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import Extensions.*;
import Services.*;
import RunLoop.*;
import Expressions.*;
import Sprites.*;
import Conditions.*;
import Actions.*;
import Expressions.*;
import Objects.*;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

public class CRunStaticText extends CRunExtension implements MouseListener {
    int styles;
    int exStyles;
    int backColor;
    int fontColor;
    CFontInfo fontInfo;
    Font font;
    int alignement;
    String text;
    JLabel lab;
    int lClickCount = -1;
    int rClickCount = -1;
    int dblClickCount = -1;
    boolean bVisible;

    public CRunStaticText() {
    }

    public int getNumberOfConditions() {
        return 4;
    }

    public boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version) {
        ho.hoImgWidth = file.readInt();
        ho.hoImgHeight = file.readInt();
        styles = file.readInt();
        exStyles = file.readInt();
        fontInfo = file.readLogFont();
        file.skipBytes(40);
        fontColor = file.readColor();
        backColor = file.readColor();
        alignement = file.readInt();
        text = file.readString();

        int hAlign = JLabel.LEFT;
        switch (alignement) {
//			case 0:
//	    		align=JLabel.LEFT;
//	    		break;
            case 1:
                hAlign = JLabel.CENTER;
                break;
            case 2:
                hAlign = JLabel.RIGHT;
                break;
        }
        int vAlign = JLabel.TOP;
        if ((styles & 0x00000200) != 0) {
            vAlign = JLabel.CENTER;
        }
        lab = new JLabel(text, hAlign);
        lab.setVerticalAlignment(vAlign);
        lab.setOpaque(true);
        font = fontInfo.createFont();
        lab.setFont(font);
        lab.setBackground(new Color(backColor));
        lab.setForeground(new Color(fontColor));

        // The Borders
        if ((exStyles & 0x00000200) != 0)        	// WS_EX_CLIENTEDGE
        {
            lab.setBorder(BorderFactory.createLoweredBevelBorder());
        } else if ((exStyles & 0x00020000) != 0)  	// WS_EX_STATICEDGE
        {
            lab.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        } else if ((styles & 0x00800000) != 0)    	// WS_BORDER
        {
            lab.setBorder(BorderFactory.createLineBorder(Color.black));
        }
        // WS_VISIBLE
        bVisible = (styles & 0x10000000) != 0;
        lab.setVisible(bVisible);
        ho.getJPanel().add(lab);
        lab.setBounds(ho.scaleX(ho.getX()), ho.scaleY(ho.getY()), ho.scaleX(ho.getWidth()), ho.scaleY(ho.getHeight()));
        lab.repaint();
        lab.addMouseListener(this);

        return false;
    }

    public void destroyRunObject(boolean bFast) {
        ho.getJPanel().remove(lab);
    }

    public int handleRunObject() {
        return 0;
    }

    public void displayRunObject(Graphics2D g2) {
        lab.setBounds(ho.scaleX(ho.getX()), ho.scaleY(ho.getY()), ho.scaleX(ho.getWidth()), ho.scaleY(ho.getHeight()));
        lab.repaint();
    }

    public void pauseRunObject() {
    }

    public void continueRunObject() {
    }

    public boolean saveRunObject(DataOutputStream stream) {
        try {
            stream.writeShort(1);        	// Version
            stream.writeInt(backColor);
            stream.writeInt(fontColor);
            stream.writeInt(lClickCount);
            stream.writeInt(rClickCount);
            stream.writeInt(dblClickCount);
            stream.writeBoolean(bVisible);
            fontInfo.write(stream);
            stream.writeUTF(text);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean loadRunObject(DataInputStream stream) {
        try {
            short v = stream.readShort();        // Version
            if (v != 1) {
                return false;
            }
            backColor = stream.readInt();
            fontColor = stream.readInt();
            lClickCount = stream.readInt();
            rClickCount = stream.readInt();
            dblClickCount = stream.readInt();
            bVisible = stream.readBoolean();
            fontInfo.read(stream);
            text = stream.readUTF();

            // Create the object
            font = fontInfo.createFont();
            lab.setFont(font);
            lab.setBackground(new Color(backColor));
            lab.setForeground(new Color(fontColor));
            lab.setVisible(bVisible);
            lab.setBounds(ho.scaleX(ho.getX()), ho.scaleY(ho.getY()), ho.scaleX(ho.getWidth()), ho.scaleY(ho.getHeight()));
            lab.repaint();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void saveBackground(BufferedImage img) {
    }

    public void restoreBackground(Graphics2D g) {
    }

    public void killBackground() {
    }

    public CFontInfo getRunObjectFont() {
        return fontInfo;
    }

    public void setRunObjectFont(CFontInfo fi, CRect rc) {
        fontInfo = fi;
        font = fi.createFont();
        lab.setFont(font);
        if (rc != null) {
            ho.setWidth(rc.right);
            ho.setHeight(rc.bottom);
            lab.setBounds(ho.scaleX(ho.getX()), ho.scaleY(ho.getY()), ho.scaleX(ho.getWidth()), ho.scaleY(ho.getHeight()));
        }
        lab.repaint();
    }

    public int getRunObjectTextColor() {
        return fontColor;
    }

    public void setRunObjectTextColor(int rgb) {
        fontColor = rgb;
        lab.setForeground(new Color(fontColor));
        lab.repaint();
    }

    public CMask getRunObjectCollisionMask(int flags) {
        return null;
    }

    public BufferedImage getRunObjectSurface() {
        return null;
    }

    public void getZoneInfos() {
    }

    // Listeners
    // -------------------------------------------------
    public void mousePressed(MouseEvent e) {
        int a = 2;
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        int nClicks = e.getClickCount();
        int button = e.getButton();
        if (button == MouseEvent.BUTTON1) {
            if (nClicks == 1) {
                lClickCount = ho.getEventCount();
                ho.pushEvent(0, 0);        // CND_LCLICK
            } else if (nClicks == 2) {
                dblClickCount = ho.getEventCount();
                ho.pushEvent(2, 0);        // CND_DBLCLICK
            }
        } else if (button == MouseEvent.BUTTON3) {
            if (nClicks == 1) {
                rClickCount = ho.getEventCount();
                ho.pushEvent(1, 0);        // CND_RCLICK
            }
        }
    }

    // Conditions
    // --------------------------------------------------
    public boolean condition(int num, CCndExtension cnd) {
        switch (num) {
            case 0:
                return cndLClick(cnd);
            case 1:
                return cndRClick(cnd);
            case 2:
                return cndDblClick(cnd);
            case 3:
                return cndIsVisible(cnd);
        }
        return false;
    }

    boolean cndLClick(CCndExtension cnd) {
        // If the condition is placed first: always true!
        if ((ho.hoFlags & CObject.HOF_TRUEEVENT) != 0)
            return true;

        // If the condition is secondary, check the loop number.
        return lClickCount == ho.getEventCount();
    }

    boolean cndRClick(CCndExtension cnd) {
        // If the condition is placed first: always true!
        if ((ho.hoFlags & CObject.HOF_TRUEEVENT) != 0)
            return true;

        // If the condition is secondary, check the loop number.
        return rClickCount == ho.getEventCount();
    }

    boolean cndDblClick(CCndExtension cnd) {
        // If the condition is placed first: always true!
        if ((ho.hoFlags & CObject.HOF_TRUEEVENT) != 0)
            return true;

        // If the condition is secondary, check the loop number.
        return dblClickCount == ho.getEventCount();
    }

    boolean cndIsVisible(CCndExtension cnd) {
        return bVisible;
    }

    // Actions
    // -------------------------------------------------
    public void action(int num, CActExtension act) {
        switch (num) {
            case 0:
                actHide(act);
                break;
            case 1:
                actShow(act);
                break;
            case 2:
                actSetWidth(act);
                break;
            case 3:
                actSetHeight(act);
                break;
            case 4:
                actSetText(act);
                break;
            case 5:
                actSetTextColor(act);
                break;
            case 6:
                actSetBackColor(act);
                break;
        }
    }

    void actHide(CActExtension act) {
        bVisible = false;
        lab.setVisible(bVisible);
    }

    void actShow(CActExtension act) {
        bVisible = true;
        lab.setVisible(bVisible);
        lab.repaint();
    }

    void actSetWidth(CActExtension act) {
        int width = act.getParamExpression(rh, 0);
        if (width > 0) {
            ho.setWidth(width);
            ho.redraw();
        }
    }

    void actSetHeight(CActExtension act) {
        int height = act.getParamExpression(rh, 0);
        if (height > 0) {
            ho.setHeight(height);
            ho.redraw();
        }
    }

    void actSetText(CActExtension act) {
        text = act.getParamExpString(rh, 0);
        lab.setText(text);
        lab.repaint();
    }

    void actSetTextColor(CActExtension act) {
        int color = act.getParamExpression(rh, 0);
        lab.setForeground(new Color(color));
    }

    void actSetBackColor(CActExtension act) {
        int color = act.getParamExpression(rh, 0);
        lab.setBackground(new Color(color));
    }

    // Expressions
    // --------------------------------------------
    public CValue expression(int num) {
        switch (num) {
            case 0:
                return expGetWidth();
            case 1:
                return expGetHeight();
            case 2:
                return expGetText();
        }
        return null;
    }

    CValue expGetWidth() {
        return new CValue(ho.getWidth());
    }

    CValue expGetHeight() {
        return new CValue(ho.getHeight());
    }

    CValue expGetText() {
        return new CValue(text);
    }
}
