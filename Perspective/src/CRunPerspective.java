//----------------------------------------------------------------------------------
//
// CRUNPERSPECTIVE
//
//----------------------------------------------------------------------------------

import Actions.CActExtension;
import Conditions.CCndExtension;
import Expressions.CValue;
import Extensions.CRunExtension;
import RunLoop.CCreateObjectInfo;
import Services.CBinaryFile;

import java.awt.*;
import java.awt.image.VolatileImage;

@SuppressWarnings("unused")
public class CRunPerspective extends CRunExtension {
    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;

    public final static int PANORAMA = 0;
    public final static int PERSPECTIVE = 1;
    public final static int SINEWAVE = 2;
    public final static int SINEOFFSET = 3;
    public final static int CUSTOM = 4;
    public final static int CUSTOMOFFSET = 5;

    public final static int RIGHTTOP = 0;
    public final static int LEFTBOTTOM = 1;

    private final static int[] zoomRange = {0, 32767};
    private final static int[] offsetRange = {-16383, 16383};
    private final static int[] waveRange = {0, 32767};
    private final static double delta = 3.141592 / 180.0;

    public VolatileImage Buffer;
    public int Effect;
    public int Direction;
    public int Zoom;
    public int Offset;
    public int SineWaveWaves;
    public int PerspectiveDir;
    public int[] CustomArray;
    public boolean Resample;

    private int bufferWidth;
    private int bufferHeight;

    public CRunPerspective() {

    }

    private void createBuffer(GraphicsConfiguration gc, int width, int height) {
        bufferWidth = width;
        bufferHeight = height;
        Buffer = gc.createCompatibleVolatileImage(width, height, Transparency.OPAQUE);
    }

    private void validateBuffer(GraphicsConfiguration gc) {
        if (Buffer == null) {
            createBuffer(gc, bufferWidth, bufferHeight);
            return;
        }
        int status = Buffer.validate(gc);
        if (status == VolatileImage.IMAGE_INCOMPATIBLE) {
            Buffer.flush();
            createBuffer(gc, bufferWidth, bufferHeight);
        }
    }

    @Override
    public boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version) {
        // This function is called when the object is created.
        //
        // As Java object are just created when needed, there is no EDITDATA structure.
        // Instead, I send to the function a CBinaryFile object, pointing directly to
        // the data of the object (the EDITDATA structure, on disc).
        //
        // The CBinaryFile object allows you to read the data.
        // It automatically converts PC-like ordering (big or little endian
        // I cant remember) into Java ordering.
        // It contains functions to read bytes, shorts, int, colors and strings.
        // Read the documentation about this class at the end of the document.
        //
        // "Version" contains the version value contained in the extHeader
        // structure of the EDITDATA.
        //
        // So all you have to do, is read the data from the CBinaryFile object,
        // and initialise your object accordingly.

        //skip sx/sy
        file.skipBytes(4);

        //set dimensions
        short width = file.readShort();
        short height = file.readShort();
        ho.setX(cob.cobX);
        ho.setY(cob.cobY);
        ho.setWidth(width);
        ho.setHeight(height);

        //store buffer dimensions for later VolatileImage creation
        bufferWidth = width;
        bufferHeight = height;
        // Buffer will be created lazily on first displayRunObject call,
        // since we need a GraphicsConfiguration from the Graphics2D context.

        //read properties
        Effect = file.readByte();
        Direction = file.readByte() != 0 ? VERTICAL : HORIZONTAL;
        file.skipBytes(2);
        Zoom = file.readInt();
        Offset = file.readInt();
        SineWaveWaves = file.readInt();
        PerspectiveDir = file.readByte() != 0 ? LEFTBOTTOM : RIGHTTOP;
        Resample = file.readByte() != 0;

        //flag changed
        ho.roc.rcChanged = true;

        //create custom array
        int size = (Direction == HORIZONTAL) ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        CustomArray = new int[size];
        for (int i = 0; i < size; i++) {
            CustomArray[i] = Zoom;
        }

        return false;
    }

    @Override
    public void destroyRunObject(boolean bFast) {
        // Called when the object is destroyed.
        // Due to garbage collection, this routine should not have much to do,
        // as all the data reserved in the object will be freed at the next GC.
        // bFast is true if the object is destroyed at end of frame.
        // It is false if the object is destroyed in the middle of the application.
        if (Buffer != null) {
            Buffer.flush();
            Buffer = null;
        }
    }

    @Override
    public int handleRunObject() {
        // Same as the C++ function.
        // Perform all the tasks needed for your object in this function.
        // As the C function, this function returns value indicating what to do :
        //
        // - REFLAG_ONESHOT : handleRunObject will not be called anymore
        // - REFLAG_DISPLAY : displayRunObject is called at next refresh
        // - Return 0 and the handleRunObject method will be called at the next loop
        return this.ho.roc.rcChanged ? REFLAG_ONESHOT : 0;
    }

    @Override
    public void displayRunObject(Graphics2D graphics2D) {
        // Called to display the object.
        // The parameter given is Graphics2D object of the frame,
        // where you must draw your object at the correct co-ordinates.
        // For controls, this function is also called when you resize the window,
        // this time with a Graphics2D equal to null.
        //
        // When you resize the window, I explore the list of object,
        // and call the ones with a OEFLAG_WINDOWPROC defined (this should include all
        // the controls).
        //
        // You should reposition the control at the correct position (using
        // getXOffet and getYOffset).

        int x = ho.hoX - rh.rhWindowX;
        int y = ho.hoY - rh.rhWindowY;
        int width = ho.hoImgWidth;
        int height = ho.hoImgHeight;

        GraphicsConfiguration gc = graphics2D.getDeviceConfiguration();

        // create buffer lazily or recreate if lost/incompatible
        if (Buffer == null) {
            createBuffer(gc, width, height);
        }

        // render into the buffer, retry if contents are lost
        do {
            validateBuffer(gc);

            Graphics2D g2d = Buffer.createGraphics();
            g2d.drawImage(rh.rhApp.editWin, x, y, null);
            g2d.dispose();
        } while (Buffer.contentsLost());

        // Draw to the context
        Shape originalClip = graphics2D.getClip();
        graphics2D.setClip(x, y, width, height);

        switch (Effect)
        {
            case PANORAMA: {
                double calcSin;
                int calcSize;

                if (Direction == CRunPerspective.HORIZONTAL) {
                    for (int i = 0; i <= width; i++) {
                        calcSin = (i - width / 2.0) / (width / 3.1415) + (3.1415 / 2);
                        calcSize = (int) Math.max(1.0, height + (Math.sin(calcSin)) * Zoom - Zoom);

                        graphics2D.drawImage(Buffer, x + i, y, x + i + 1, y + height, i, (height / 2) - (calcSize / 2), i + 1, (height / 2) + (calcSize / 2), null);
                    }
                } else {
                    for (int i = 0; i <= height; i++) {
                        calcSin = (i - height / 2.0) / (height / 3.1415) + (3.1415 / 2);
                        calcSize = (int) Math.max(1.0, height + (Math.sin(calcSin)) * Zoom - Zoom);

                        graphics2D.drawImage(Buffer, x, y + i, x + width, y + i + 1, (width / 2) - (calcSize / 2), i, (width / 2) + (calcSize / 2), i + 1, null);
                    }
                }
                break;
            }
            case CRunPerspective.PERSPECTIVE: {
                double calcZoom;
                double calcFactor;
                double calcSize;

                if (Direction == CRunPerspective.HORIZONTAL){
                    //horizontal
                    if (PerspectiveDir == CRunPerspective.RIGHTTOP) {
                        //larger height on right
                        for (int i = 0; i <= width; i++) {
                            calcZoom = (i * (double) Zoom) / width;
                            calcFactor = (height + calcZoom) / height;
                            calcSize = (height / calcFactor + 0.5);
                            graphics2D.drawImage(Buffer, x + i, y, 1, height, i, (int) (height / 2.0 - calcSize / 2), 1, (int) calcSize, null);
                        }
                    } else {
                        //larger height on left
                        for (int i = 0; i <= width; i++) {
                            calcZoom = ((width - i - 1) * (double)Zoom) / width;
                            calcFactor = (height + calcZoom) / height;
                            calcSize = (height / calcFactor + 0.5);
                            graphics2D.drawImage(Buffer, x + i, y, 1, height, i, (int)(height / 2.0 - calcSize / 2), 1, (int)calcSize, null);
                        }
                    }
                } else {
                    //vertical
                    if (PerspectiveDir == CRunPerspective.RIGHTTOP) {
                        //larger width on bottom
                        for (int i = 0; i <= height; i++) {
                            calcZoom = (i * (double)Zoom) / height;
                            calcFactor = (width + calcZoom) / width;
                            calcSize = (width / calcFactor + 0.5);
                            graphics2D.drawImage(Buffer, x, y + i, width, 1, (int)(width / 2.0 - calcSize / 2), i, (int)calcSize, 1, null);
                        }
                    } else {
                        //larger width on top
                        for (int i = 0; i <= height; i++) {
                            calcZoom = ((height - i - 1) * (double)Zoom) / height;
                            calcFactor = (width + calcZoom) / width;
                            calcSize = (width / calcFactor + 0.5);
                            graphics2D.drawImage(Buffer, x, y + i, width, 1, (int)(width / 2.0 - calcSize / 2), i, (int)calcSize, 1, null);
                        }
                    }
                }
                break;
            }
            case CRunPerspective.SINEWAVE: {
                int calcSize;
                double waveIncrement;

                if (Direction == CRunPerspective.HORIZONTAL) {
                    //horizontal
                    waveIncrement = (SineWaveWaves * 360.0) / height;

                    for (int i = 0; i <= width; i++) {
                        calcSize = (int)Math.max(1, (height + Math.sin((i * waveIncrement + Offset) * CRunPerspective.delta) * Zoom - Zoom));
                        graphics2D.drawImage(Buffer, x + i, y, 1, height, i, height / 2 - calcSize / 2, 1, calcSize, null);
                    }
                } else {
                    //vertical
                    waveIncrement = (SineWaveWaves * 360.0) / width;

                    for (int i = 0; i <= height; i++) {
                        calcSize = (int)Math.max(1, (width + Math.sin((i * waveIncrement + Offset) * CRunPerspective.delta) * Zoom - Zoom));
                        graphics2D.drawImage(Buffer, x, y + i, width, 1, width / 2 - calcSize / 2, i, calcSize, 1, null);
                    }
                }
                break;
            }
            case CRunPerspective.SINEOFFSET: {
                double calcOffset;
                double waveIncrement;

                //black out the background
                Color originalColor = graphics2D.getColor();
                graphics2D.setColor(Color.BLACK);
                graphics2D.fillRect(x, y, width, height);
                graphics2D.setColor(originalColor);

                //render wave
                if (Direction == CRunPerspective.HORIZONTAL) {
                    //horizontal
                    waveIncrement = (SineWaveWaves * 360.0  ) / height;

                    for (int i = 0; i <= width; i++) {
                        calcOffset = Math.sin((i * waveIncrement + Offset) * CRunPerspective.delta) * Zoom;
                        graphics2D.drawImage(Buffer, x + i, (int)(y + calcOffset), 1, height, i, 0, 1, height, null);
                    }
                } else {
                    //vertical
                    waveIncrement = (SineWaveWaves * 360.0) / width;

                    for (int i = 0; i <= height; i++) {
                        calcOffset = Math.sin((i * waveIncrement + Offset) * CRunPerspective.delta) * Zoom;
                        graphics2D.drawImage(Buffer, (int)(x + calcOffset), y + i, width, 1, 0, i, width, 1, null);
                    }
                }
                break;
            }
            case CRunPerspective.CUSTOM: {
                double calcSize;

                if (Direction == CRunPerspective.HORIZONTAL) {
                    //horizontal
                    for (int i = 0; i <= width; i++) {
                        calcSize = height / ((CustomArray[i] * (Zoom / 100.0) + 100) / 100.0);
                        graphics2D.drawImage(Buffer, x + i, y, 1, height, i, (int)(height / 2.0 - calcSize / 2 + Offset), 1, (int)calcSize, null);
                    }
                } else {
                    //vertical
                    for (int i = 0; i <= height; i++) {
                        calcSize = width / ((CustomArray[i] * (Zoom / 100.0) + 100) / 100.0);
                        graphics2D.drawImage(Buffer, x, y + i, width, 1, (int)(width / 2.0 - calcSize / 2 + Offset), i, (int)calcSize, 1, null);
                    }
                }
                break;
            }
            case CRunPerspective.CUSTOMOFFSET: {
                double calcOffset;

                //black out the background
                Color originalColor = graphics2D.getColor();
                graphics2D.setColor(Color.BLACK);
                graphics2D.fillRect(x, y, width, height);
                graphics2D.setColor(originalColor);

                if (Direction == CRunPerspective.HORIZONTAL) {
                    //horizontal
                    for (int i = 0; i <= width; i++) {
                        calcOffset = (CustomArray[i] * (Zoom / 100.0)) + Offset;
                        graphics2D.drawImage(Buffer, x + i, (int)(y + calcOffset), 1, height, i, 0, 1, height, null);
                    }
                } else {
                    //vertical
                    for (int i = 0; i <= height; i++) {
                        calcOffset = (CustomArray[i] * (Zoom / 100.0)) + Offset;
                        graphics2D.drawImage(Buffer, (int)(x + calcOffset), y + i, width, 1, 0, i, width, 1, null);
                    }
                }
                break;
            }
        }

        graphics2D.setClip(originalClip);
    }

    // Conditions
    // --------------------------------------------------
    @Override
    public int getNumberOfConditions() {
        // This function should return the number of conditions contained in the object
        return 0;
    }

    @Override
    public boolean condition(int num, CCndExtension cnd) {
        // The main entry for the evaluation of the conditions.
        // - num : number of the condition (equivalent to the CND_ definitions in ext.h)
        // - cnd : a pointer to a CCndExtension object that contains useful callback
        // functions to get the parameters.
        // - This function should return true or false, depending on the condition.
        return false;
    }

    // Actions
    // -------------------------------------------------
    private final static int ACT_SETZOOMVALUE = 0;
    private final static int ACT_SETPANORAMA = 1;
    private final static int ACT_SETPERSPECTIVE = 2;
    private final static int ACT_SETSINEWAVE = 3;
    private final static int ACT_SETCUSTOM = 4;
    private final static int ACT_SETNUMWAVES = 5;
    private final static int ACT_SETOFFSET = 6;
    private final static int ACT_SETHORIZONTAL = 7;
    private final static int ACT_SETVERTICAL = 8;
    private final static int ACT_SETLEFTTOP = 9;
    private final static int ACT_SETRIGHTBOTTOM = 10;
    private final static int ACT_SETCUSTOMVALUE = 11;
    private final static int ACT_SETWIDTH = 12;
    private final static int ACT_SETHEIGHT = 13;
    private final static int ACT_SETRESAMPLEON = 14;
    private final static int ACT_SETRESAMPLEOFF = 15;
    private final static int ACT_SETSINEOFFSET = 16;
    private final static int ACT_SETCUSTOMOFFSET = 17;

    @Override
    public void action(int num, CActExtension act) {
        // The main entry for the actions.
        // - num : number of the action, as defined in ext.h
        // - act : pointer to a CActExtension object that contains callback
        // functions to get the parameters.
        switch (num) {
            case ACT_SETZOOMVALUE:
                int zoom = act.getParamExpression(rh, 0);
                actSetZoomValue(zoom);
                break;
            case ACT_SETPANORAMA:
                actSetPanorama();
                break;
            case ACT_SETPERSPECTIVE:
                actSetPerspective();
                break;
            case ACT_SETSINEWAVE:
                actSetSineWave();
                break;
            case ACT_SETCUSTOM:
                actSetCustom();
                break;
            case ACT_SETNUMWAVES:
                int waves = act.getParamExpression(rh, 0);
                actSetNumWaves(waves);
                break;
            case ACT_SETOFFSET:
                int offset = act.getParamExpression(rh, 0);
                actSetOffset(offset);
                break;
            case ACT_SETHORIZONTAL:
                actSetHorizontal();
                break;
            case ACT_SETVERTICAL:
                actSetVertical();
                break;
            case ACT_SETLEFTTOP:
                actSetLeftTop();
                break;
            case ACT_SETRIGHTBOTTOM:
                actSetRightBottom();
                break;
            case ACT_SETCUSTOMVALUE:
                int i = act.getParamExpression(rh, 0);
                int value = act.getParamExpression(rh, 1);
                actSetCustomValue(i, value);
                break;
            case ACT_SETWIDTH:
                int width = act.getParamExpression(rh, 0);
                actSetWidth(width);
                break;
            case ACT_SETHEIGHT:
                int height = act.getParamExpression(rh, 0);
                actSetHeight(height);
                break;
            case ACT_SETRESAMPLEON:
            case ACT_SETRESAMPLEOFF:
                // Not implemented
                break;
            case ACT_SETSINEOFFSET:
                actSetSineOffset();
                break;
            case ACT_SETCUSTOMOFFSET:
                actSetCustomOffset();
                break;
        }
    }

    public void actSetZoomValue(int zoom)
    {
        Zoom = zoom;
        ho.roc.rcChanged = true;
    }

    public void actSetPanorama()
    {
        Effect = PANORAMA;
        ho.roc.rcChanged = true;
    }

    public void actSetPerspective()
    {
        Effect = PERSPECTIVE;
        ho.roc.rcChanged = true;
    }

    public void actSetSineWave()
    {
        Effect = SINEWAVE;
        ho.roc.rcChanged = true;
    }

    public void actSetCustom()
    {
        Effect = CUSTOM;
        ho.roc.rcChanged = true;
    }

    public void actSetSineOffset()
    {
        Effect = SINEOFFSET;
        ho.roc.rcChanged = true;
    }

    public void actSetCustomOffset()
    {
        Effect = CUSTOMOFFSET;
        ho.roc.rcChanged = true;
    }

    public void actSetNumWaves(int waves)
    {
        SineWaveWaves = waves;
        ho.roc.rcChanged = true;
    }

    public void actSetOffset(int offset)
    {
        Offset = offset;
        ho.roc.rcChanged = true;
    }

    public void actSetHorizontal()
    {
        int oldSize = (Direction == CRunPerspective.HORIZONTAL) ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int newSize = this.ho.hoImgWidth;

        Direction = CRunPerspective.HORIZONTAL;
        this.ho.roc.rcChanged = true;

        int minSize = Math.min(oldSize, newSize);
        int[] newCustom = new int[minSize];

        System.arraycopy(CustomArray, 0, newCustom, 0, minSize);

        CustomArray = newCustom;
    }

    public void actSetVertical()
    {
        int oldSize = (Direction == CRunPerspective.HORIZONTAL) ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int newSize = this.ho.hoImgHeight;

        Direction = CRunPerspective.VERTICAL;
        this.ho.roc.rcChanged = true;

        int minSize = Math.min(oldSize, newSize);
        int[] newCustom = new int[minSize];

        System.arraycopy(CustomArray, 0, newCustom, 0, minSize);

        CustomArray = newCustom;
    }

    public void actSetLeftTop()
    {
        PerspectiveDir = RIGHTTOP;
        this.ho.roc.rcChanged = true;
    }

    public void actSetRightBottom()
    {
        PerspectiveDir = LEFTBOTTOM;
        this.ho.roc.rcChanged = true;
    }

    public void actSetCustomValue(int i, int value)
    {
        int size = (Direction == CRunPerspective.HORIZONTAL) ? this.ho.hoImgWidth : this.ho.hoImgHeight;

        if (i >= 0 && i < size) {
            CustomArray[i] = value;
        }

        this.ho.roc.rcChanged = true;
    }

    public void actSetWidth(int newWidth)
    {
        int oldSize = (Direction == CRunPerspective.HORIZONTAL) ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        this.ho.setWidth(newWidth);
        this.resizePerspective(oldSize);
    }

    public void actSetHeight(int newHeight)
    {
        int oldSize = (Direction == CRunPerspective.HORIZONTAL) ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        this.ho.setHeight(newHeight);
        this.resizePerspective(oldSize);
    }

    // Expressions
    // --------------------------------------------
    private final static int EXP_GETZOOMVALUE = 0;
    private final static int EXP_GETOFFSET = 1;
    private final static int EXP_NUMWAVES = 2;
    private final static int EXP_GETCUSTOM = 3;
    private final static int EXP_GETWIDTH = 4;
    private final static int EXP_GETHEIGHT = 5;

    @Override
    public CValue expression(int num) {
        // The main entry for expressions.
        // - num : number of the expression
        //
        // To get the expression parameters, you have to call the getExpParam() method
        // defined in the "ho" variable, for each of the parameters.
        // This function returns a CValue which contains the parameter.
        // You then do a getInt(), getDouble() or getString() with the
        // CValue object to grab the actual value.
        //
        // This method should return a CValue object containing the value to return.
        // The content of the CValue can be a integer, a double or a String.
        // There is no need to set the HOF_STRING flags if your return a string :
        // the CValue object contains the type of the returned value.
        switch (num) {
            case EXP_GETZOOMVALUE:
                return new CValue(expGetZoomValue());
            case EXP_GETOFFSET:
                return new CValue(expGetOffset());
            case EXP_NUMWAVES:
                return new CValue(expNumWaves());
            case EXP_GETCUSTOM:
                int min = ho.getExpParam().intValue;
                return new CValue(expGetCustom(min));
            case EXP_GETWIDTH:
                return new CValue(expGetWidth());
            case EXP_GETHEIGHT:
                return new CValue(expGetHeight());
        }
        return null;
    }

    public int expGetZoomValue()
    {
        return Zoom;
    }

    public int expGetOffset()
    {
        return Offset;
    }

    public int expNumWaves()
    {
        return SineWaveWaves;
    }

    public int expGetCustom(int min)
    {
        int size = (Direction == HORIZONTAL) ? this.ho.hoImgWidth : this.ho.hoImgHeight;

        return CustomArray[Math.min(Math.max(0, min), size - 1)];
    }

    public int expGetWidth()
    {
        return ho.hoImgWidth;
    }

    public int expGetHeight()
    {
        return ho.hoImgHeight;
    }

    // Internal
    public void resizePerspective(int oldSize)
    {
        int size = (Direction == CRunPerspective.HORIZONTAL) ? this.ho.hoImgWidth : this.ho.hoImgHeight;

        //has teh size changed?
        if (size != oldSize) {
            //recreates the buffer the next time it is drawn with the new size
            Buffer = null;

            //recreate custom array
            int[] newArray = new int[size];
            int numNewInts = Math.min(size, oldSize);

            for (int i = 0; i < size; i++) {
                if (i < numNewInts) {
                    newArray[i] = CustomArray[i];
                } else {
                    newArray[i] = 0;
                }
            }

            //save array
            CustomArray = newArray;

            //flag changed
            this.ho.roc.rcChanged = true;
        }
    }
}