//----------------------------------------------------------------------------------
//
// CRUNTEMPLATE: Template extension object by Yunivers
//
//----------------------------------------------------------------------------------

import Actions.CActExtension;
import Conditions.CCndExtension;
import Expressions.CValue;
import Extensions.CRunExtension;
import Params.CParamExpression;
import RunLoop.CCreateObjectInfo;
import Services.CBinaryFile;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class CRunDarkEdif_Template extends CRunExtension  {
    // DarkEdif SDK exts should have these four variables defined.
    public final int ExtensionVersion = 1; // To match C++ version
    public final int SDKVersion = 20; // To match C++ version
    public final boolean DebugMode = true;
    public final String ExtensionName = "DarkEdif Template";

    // You can put variables exclusive to this extension here.
    public boolean checkboxWithinFolder;
    public String editable6Text;

    public CRunDarkEdif_Template() {
        DarkEdif.checkSupportsSDKVersion(SDKVersion);
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

        // DarkEdif properties are accessible as on other platforms: IsPropChecked(), GetPropertyStr(), GetPropertyNum()
        DarkEdif.DarkEdifProperties props = DarkEdif.getProperties(this, file, version);

        StringBuilder str = new StringBuilder();
        str.append("Looping set Fred:\n");
        for (DarkEdif.DarkEdifPropSetIterator iter = props.LoopPropSet("Fred"); iter.hasNext(); ) {
            str .append("Set entry \"")
                .append(props.GetPropertyStr("Set name"))
                .append("\" has fruit \"")
                .append(props.GetPropertyStr("This set's fruit"))
                .append("\".\n");
        }
        str.append("And agaiN!\n");
        for (DarkEdif.DarkEdifPropSetIterator iter = props.LoopPropSet("Fred"); iter.hasNext(); ) {
            str .append("Set entry \"")
                .append(props.GetPropertyStr("Set name"))
                .append("\" has fruit \"")
                .append(props.GetPropertyStr("This set's fruit"))
                .append("\".\n");
        }
        DarkEdif.consoleLog(this, "DarkEdif prop notif:\n" + str + "========\n");

        this.checkboxWithinFolder = props.IsPropChecked("Checkbox within folder");
        //this.editable6Text = props.GetPropertyStr("Editable 6");
        return false;
    }

    @Override
    public void destroyRunObject(boolean bFast) {
        // Called when the object is destroyed.
        // Due to garbage collection, this routine should not have much to do,
        // as all the data reserved in the object will be freed at the next GC.
        // bFast is true if the object is destroyed at end of frame.
        // It is false if the object is destroyed in the middle of the application.
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
	    return REFLAG_ONESHOT;
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
    }

    // Conditions
    // --------------------------------------------------
    private final static int CND_AreTwoNumbersEqual = 0;
    private final static int CND_Last = 1;

    @Override
    public int getNumberOfConditions() {
        // This function should return the number of conditions contained in the object
        return CND_Last;
    }

    @Override
    public boolean condition(int num, CCndExtension cnd) {
        // The main entry for the evaluation of the conditions.
        // - num : number of the condition (equivalent to the CND_ definitions in ext.h)
        // - cnd : a pointer to a CCndExtension object that contains useful callback
        // functions to get the parameters.
        // - This function should return true or false, depending on the condition.
        //noinspection SwitchStatementWithTooFewBranches
        switch (num) {
            case CND_AreTwoNumbersEqual: {
                double first = rh.get_EventExpressionDouble((CParamExpression)cnd.evtParams[0]);
                double second = rh.get_EventExpressionDouble((CParamExpression)cnd.evtParams[1]);
                return cndAreTwoNumbersEqual(first, second);
            }
        }
	    return false;
    }

    public boolean cndAreTwoNumbersEqual(double first, double second) {
        return first == second;
    }
    
    // Actions
    // -------------------------------------------------
    private final static int ACT_ActionExample = 0;
    private final static int ACT_SecondActionExample = 1;

    @Override
    public void action(int num, CActExtension act) {
        // The main entry for the actions.
        // - num : number of the action, as defined in ext.h
        // - act : pointer to a CActExtension object that contains callback
        // functions to get the parameters.
        switch (num) {
            case ACT_ActionExample: {
                int exampleParameter = act.getParamExpression(rh, 0);
                actActionExample(exampleParameter);
                break;
            }
            case ACT_SecondActionExample: {
                actSecondActionExample();
                break;
            }
        }
    }

    public void actActionExample(int exampleParameter) {
        // nothing, as C++ does nothing
    }

    public void actSecondActionExample() {
        // nothing, as C++ does nothing
    }

    // Expressions
    // --------------------------------------------
    private final static int EXP_Add = 0;
    private final static int EXP_HelloWorld = 1;

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
            case EXP_Add: {
                double first = ho.getExpParam().doubleValue;
                double second = ho.getExpParam().doubleValue;
                return new CValue(expAdd(first, second));
            }
            case EXP_HelloWorld: {
                return new CValue(expHelloWorld());
            }
        }
	    return null;
    }

    public double expAdd(double first, double second) {
        return first + second;
    }

    public String expHelloWorld() {
        return "Hello world!";
    }

    private static class DarkEdif {
        public static class DarkEdifProperty {
            public long index;
            public long propTypeID;
            public long propJSONIndex;
            public String propName;
            public ByteBuffer propData;

            public DarkEdifProperty(long index, long propTypeID, long propJSONIndex, String propName, ByteBuffer propData) {
                this.index = index;
                this.propTypeID = propTypeID;
                this.propJSONIndex = propJSONIndex;
                this.propName = propName;
                this.propData = propData;
            }
        }

        public static class DarkEdifPropSet {
            public String setIndicator;
            public long numRepeats;
            public long lastSetJSONPropIndex;
            public long firstSetJSONPropIndex;
            public long setNameJSONPropIndex;
            public String setName;

            private final ByteBuffer rsDV;

            public DarkEdifPropSet(ByteBuffer rsDV) {
                // Always 'S', compared with 'L' for non-set list.
                if (rsDV.remaining() > 0) {
                    setIndicator = String.valueOf((char)(rsDV.get() & 0xFF));
                }
                // Number of repeats of this set, 1 is minimum and means one of this set
                numRepeats = rsDV.getShort() & 0xFFFF;
                // Property that ends this set's data, as a JSON index, inclusive
                lastSetJSONPropIndex = rsDV.getShort() & 0xFFFF;
                // First property that begins this set's data, as a JSON index
                firstSetJSONPropIndex = rsDV.getShort() & 0xFFFF;
                // Name property JSON index that will appear in list when switching set entry
                setNameJSONPropIndex = rsDV.getShort() & 0xFFFF;
                // Set name, as specified in JSON. Don't confuse with user-specified set name.
                long bytesAvailable = rsDV.limit() - rsDV.position();
                byte[] bytes = new byte[(int)bytesAvailable];
                rsDV.get(bytes);
                try {
                    setName = new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(DarkEdifPropSet.class.getName()).log(Level.SEVERE, null, ex);
                }

                this.rsDV = rsDV;
            }

            public long getIndexSelected() {
                rsDV.position(1 + (2 * 4));
                return rsDV.getShort() & 0xFFFF;
            }

            public void setIndexSelected(long i) {
                rsDV.position(1 + (2 * 4));
                rsDV.putShort((short)i);
            }
        }

        public static class DarkEdifPropSetIterator implements Iterator<Integer> {
            public final int nameListJSONIdx;
            public final int numSkippedSetsBefore;
            public final DarkEdifProperties props;
            public final DarkEdifProperty runSetEntry;
            public final DarkEdifPropSet runPropSet;
            public boolean firstIt;

            public DarkEdifPropSetIterator(int nameListJSONIdx, int numSkippedSetsBefore, DarkEdifProperty runSetEntry, DarkEdifProperties props) {
                this.nameListJSONIdx = nameListJSONIdx;
                this.numSkippedSetsBefore = numSkippedSetsBefore;
                this.props = props;
                this.runSetEntry = runSetEntry;

                this.runPropSet = new DarkEdifPropSet(runSetEntry.propData);
                this.runPropSet.setIndexSelected(0);
                this.firstIt = true;
            }

            @Override
            public boolean hasNext() {
                return runPropSet.getIndexSelected() < runPropSet.numRepeats;
            }

            @Override
            public Integer next() {
                if (firstIt) {
                    firstIt = false;
                }
                else {
                    runPropSet.setIndexSelected(runPropSet.getIndexSelected() + 1);
                }
                return (int)runPropSet.getIndexSelected();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("You cannot remove a PropSet from this iterator!");
            }
        }

        public static class DarkEdifProperties {
            private final long numProps;
            private final int propVer;
            private final byte[] chkboxes;
            private final List<DarkEdifProperty> props;

            private static int GetFileLength(CBinaryFile file) {
                // In the Java runtime, data is private meaning DarkEdif cannot get the length of the file
                // To combat this, we use reflection to make the data accessible and grab the length
                try {
                    java.lang.reflect.Field field = CBinaryFile.class.getDeclaredField("data");
                    field.setAccessible(true);
                    byte[] data = (byte[]) field.get(file);
                    return data.length;
                } catch (Exception ex) {
                    Logger.getLogger(DarkEdifProperties.class.getName()).log(Level.SEVERE, null, ex);
                }
                return 0;
            }

            public DarkEdifProperties(CRunExtension ext, CBinaryFile edPtrFile, int extVersion) {
                // DarkEdif SDK stores offset of DarkEdif props away from start of EDITDATA inside private data.
                // eHeader is 20 bytes, so this should be 20+ bytes.
                if (ext.ho.privateData < 20) {
                    throw new RuntimeException("Not smart properties - eHeader missing?");
                }
                // DarkEdif SDK header read:
                // header uint32, hash uint32, hashtypes uint32, numprops uint16, pad uint16, sizeBytes uint32 (includes whole EDITDATA)
                // if prop set v2, then uint64 editor checkbox ptr
                // then checkbox list, one bit per checkbox, including non-checkbox properties
                // so skip numProps / 8 bytes
                // then moving to Data list:
                // size uint32 (includes whole Data), propType uint16, propNameSize uint8, propname u8 (lowercased), then data bytes

                int oldPos = edPtrFile.getFilePointer();
                byte[] bytes = new byte[GetFileLength(edPtrFile)];
                edPtrFile.seek(0);
                edPtrFile.read(bytes);
                edPtrFile.seek(oldPos);
                byte[] verBuff = new byte[4];
                edPtrFile.skipBytes(ext.ho.privateData - 20); // sub size of eHeader; edPtrFile won't start with eHeader
                edPtrFile.read(verBuff);
                String verStr = new StringBuilder(new String(verBuff)).reverse().toString();
                if (verStr.equals("DAR2")) {
                    propVer = 2;
                }
                else if (verStr.equals("DAR1")) {
                    propVer = 1;
                }
                else {
                    throw new RuntimeException("Version string " + verStr + " unknown. Did you restore the file offset?");
                }
                // Pull out hash, hashTypes, numProps, pad, sizeBytes, visibleEditorProps
                byte[] headerBytes = new byte[4 + 4 + 2 + 2 + 4 + (propVer > 1 ? 8 : 0)];
                edPtrFile.read(headerBytes);
                ByteBuffer header = ByteBuffer.wrap(headerBytes);
                header.order(ByteOrder.LITTLE_ENDIAN);
                header.position(4 + 4); // Skip past hash and hashTypes
                numProps = header.getShort() & 0xFFFF;
                header.position(4 + 4 + 4); // skip past numProps and pad
                long sizeBytes = header.getInt() & 0xFFFFFFFFL;

                byte[] editDataBytes = new byte[
                    (int)sizeBytes -
                    // skip eHeader
                    ext.ho.privateData -
                    // cursor offset
                    4 -
                    // Skip DarkEdif header
                    header.limit()
                ];
                edPtrFile.read(editDataBytes);
                ByteBuffer editData = ByteBuffer.wrap(editDataBytes);
                editData.order(ByteOrder.LITTLE_ENDIAN);
                editData.position(0);
                chkboxes = new byte[(int)Math.ceil(numProps / 8.0)];
                editData.get(chkboxes, 0, chkboxes.length);

                props = new ArrayList<DarkEdifProperty>();
                editData.position(chkboxes.length);
                ByteBuffer data = editData.slice();
                data.order(ByteOrder.LITTLE_ENDIAN);

                // Dont need TextDecoder

                long propSize;
                long propEnd;
                data.position(0); // pt
                for (long i = 0; i < numProps; ++i) {
                    propSize = data.getInt() & 0xFFFFFFFFL;
                    propEnd = data.position() - 4 + propSize;
                    long propTypeID = data.getShort() & 0xFFFF;
                    // propJSONIndex does not exist in Data in DarkEdif smart props ver 1, so JSON index is same as prop index
                    long propJSONIndex = i;
                    if (propVer == 2) {
                        propJSONIndex = data.getShort() & 0xFFFF;
                    }
                    int propNameLength = data.get() & 0xFF;
                    byte[] propNameBytes = new byte[propNameLength];
                    data.get(propNameBytes);
                    String propName = "";
                    try {
                        propName = new String(propNameBytes, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(DarkEdifProperties.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    byte[] propDataBytes = new byte[(int)(propEnd - data.position())];
                    data.get(propDataBytes);
                    ByteBuffer propData = ByteBuffer.wrap(propDataBytes);
                    propData.order(ByteOrder.LITTLE_ENDIAN);

                    props.add(new DarkEdifProperty(i, propTypeID, propJSONIndex, propName, propData));
                    data.position((int)propEnd);
                }
            }

            public boolean IsComboBoxProp(int propTypeID) {
                // PROPTYPE_COMBOBOX, PROPTYPE_COMBOBOXBTN, PROPTYPE_ICONCOMBOBOX
                return propTypeID == 7 || propTypeID == 20 || propTypeID == 24;
            }

            public DarkEdifPropSet RuntimePropSet(DarkEdifProperty data) {
                DarkEdifPropSet rs = new DarkEdifPropSet(data.propData);
                if (!rs.setIndicator.equals("S")) {
                    throw new RuntimeException("Not a prop set!");
                }
                return rs;
            }

            public int GetPropertyIndex(Object chkIDOrName) {
                if (propVer > 1) {
                    int jsonIdx;
                    DarkEdifProperty p = null;
                    if (chkIDOrName instanceof Integer || chkIDOrName instanceof Long || chkIDOrName instanceof Float || chkIDOrName instanceof Double) {
                        long id = ((Number)chkIDOrName).longValue();
                        for (DarkEdifProperty darkEdifProperty : props) {
                            if (darkEdifProperty.index == id) {
                                p = darkEdifProperty;
                                break;
                            }
                        }
                    }
                    else {
                        for (DarkEdifProperty prop : props) {
                            if (prop.propName.equals(chkIDOrName.toString())) {
                                p = prop;
                                break;
                            }
                        }
                    }
                    if (p == null) {
                        throw new RuntimeException("Invalid property name \"" + chkIDOrName + "\"");
                    }
                    jsonIdx = (int)p.propJSONIndex;

                    // Look up prop index from JSON index - DarkEdif::Properties::PropIdxFromJSONIdx
                    DarkEdifProperty data = props.get(0);
                    int i = 0;
                    while (data.propJSONIndex != jsonIdx) {
                        if (i >= numProps) {
                            throw new RuntimeException("Couldn't find property of JSON ID " + jsonIdx + ", hit property " + i + " of " + numProps + " stored.\n");
                        }

                        char propDataIdentifier = 0;
                        if (data.propData.remaining() > 0) {
                            propDataIdentifier = (char)data.propData.get(0);
                        }
                        if (IsComboBoxProp((int)data.propTypeID) && propDataIdentifier == 'S') {
                            DarkEdifPropSet rs = RuntimePropSet(data);
                            if (jsonIdx > rs.lastSetJSONPropIndex) {
                                while (data.propJSONIndex != rs.lastSetJSONPropIndex) {
                                    data = props.get(i++);
                                }
                            }
                            // It's within this set's range
                            else if (jsonIdx >= rs.firstSetJSONPropIndex && jsonIdx <= rs.lastSetJSONPropIndex) {
                                if (rs.getIndexSelected() > 0) {
                                    int j = 0;
                                    while (true) {
                                        data = props.get(++i);

                                        // Skip until end of this entry, then move to next prop
                                        if (data.propJSONIndex == rs.lastSetJSONPropIndex) {
                                            if (++j == rs.getIndexSelected()) {
                                                data = props.get(++i);
                                                break;
                                            }
                                        }
                                    }
                                }
                                else {
                                    data = props.get(++i);
                                }
                                continue;
                            }
                            // else it's not in this set: continue to standard loop
                        }

                        data = props.get(++i);
                    }
                    return (int)data.index;
                }
                if (chkIDOrName instanceof Integer || chkIDOrName instanceof Long || chkIDOrName instanceof Float || chkIDOrName instanceof Double) {
                    long id = ((Number)chkIDOrName).longValue();
                    if (numProps <= id) {
                        throw new RuntimeException("Invalid property ID " + chkIDOrName + ", max ID is " + (numProps - 1) + ".");
                    }
                    return (int)id;
                }
                DarkEdifProperty p2 = null;
                for (DarkEdifProperty prop : props) {
                    if (prop.propName.equals(chkIDOrName.toString())) {
                        p2 = prop;
                        break;
                    }
                }
                if (p2 == null) {
                    throw new RuntimeException("Invalid property name \"" + chkIDOrName + "\"");
                }
                return (int)p2.index;
            }

            public boolean IsPropChecked(Object chkIDOrName) {
                int idx = GetPropertyIndex(chkIDOrName);
                if (idx == -1) {
                    return false;
                }
                return (chkboxes[(int)Math.floor(idx / 8.0)] & (1 << idx % 8)) != 0;
            }

            static final List<Integer> textPropIDs = Arrays.asList(
                5,  // PROPTYPE_EDIT_STRING
                22, // PROPTYPE_EDIT_MULTILINE
                16, // PROPTYPE_FILENAME
                19, // PROPTYPE_PICTUREFILENAME
                26 // PROPTYPE_DIRECTORYNAME
            );

            public String GetPropertyStr(Object chkIDOrName) {
                int idx = GetPropertyIndex(chkIDOrName);
                if (idx == -1) {
                    return null;
                }
                DarkEdifProperty prop = props.get(idx);
                if (textPropIDs.contains((int)prop.propTypeID) || IsComboBoxProp((int)prop.propTypeID)) {
                    // Prop ver 2 added repeating prop sets
                    if (propVer == 2 && IsComboBoxProp((int)prop.propTypeID)) {
                        char setIndicator = 0;
                        if (prop.propData.remaining() > 0) {
                            setIndicator = (char)prop.propData.get(0);
                        }
                        if (setIndicator == 'L') {
                            prop.propData.position(1);
                            byte[] propStrBytes = new byte[prop.propData.remaining()];
                            prop.propData.get(propStrBytes);
                            try {
                                return new String(propStrBytes, "UTF-8");
                            } catch (UnsupportedEncodingException ex) {
                                Logger.getLogger(DarkEdifProperties.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            return "";
                        }
                        else if (setIndicator == 'S') {
                            throw new RuntimeException("Property " + prop.propName + " is not textual.");
                        }
                        throw new RuntimeException("Property " + prop.propName + " is not a valid list property.");
                    }
                    prop.propData.position(0);
                    byte[] tBytes = new byte[prop.propData.remaining()];
                    prop.propData.get(tBytes);
                    String t = "";
                    try {
                        t = new String(tBytes, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(DarkEdifProperties.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (prop.propTypeID == 22) { //PROPTYPE_EDIT_MULTILINE
                        t = t.replace("\r", ""); // CRLF to LF
                    }
                    return t;
                }
                throw new RuntimeException("Property " + prop.propName + " is not textual.");
            }

            static final List<Integer> numPropIDsInteger = Arrays.asList(
                6, // PROPTYPE_EDIT_NUMBER
                9, // PROPTYPE_COLOR
                11, // PROPTYPE_SLIDEREDIT
                12, // PROPTYPE_SPINEDIT
                13 // PROPTYPE_DIRCTRL
            );

            static final List<Integer> numPropIDsFloat = Arrays.asList(
                21, // PROPTYPE_EDIT_FLOAT
                27 // PROPTYPE_SPINEDITFLOAT
            );

            public double GetPropertyNum(Object chkIDOrName) {
                int idx = GetPropertyIndex(chkIDOrName);
                if (idx == -1) {
                    return 0.0;
                }
                DarkEdifProperty prop = props.get(idx);
                if (numPropIDsInteger.contains((int)prop.propTypeID)) {
                    return prop.propData.getInt(0) & 0xFFFFFFFFL;
                }
                if (numPropIDsFloat.contains((int)prop.propTypeID)) {
                    return prop.propData.getFloat(0);
                }
                throw new RuntimeException("Property " + prop.propName + " is not numeric.");
            }

            public int GetPropertyImageID(Object chkIDOrName, int imgID) {
                int idx = GetPropertyIndex(chkIDOrName);
                if (idx == -1) {
                    return -1;
                }
                DarkEdifProperty prop = props.get(idx);
                if (prop.propTypeID != 23) { // PROPTYPE_IMAGELIST
                    throw new RuntimeException("Property " + prop.propName + " is not an image list.");
                }

                if (imgID < 0){
                    throw new RuntimeException("Image index " + imgID + " is invalid.");
                }
                if (imgID >= (prop.propData.getShort(0) & 0xFFFF)){
                    return -1;
                }

                return prop.propData.getShort(2 * (1 + idx)) & 0xFFFF;
            }

            public int GetPropertyNumImages(Object chkIDOrName, int imgID) {
                int idx = GetPropertyIndex(chkIDOrName);
                if (idx == -1) {
                    return -1;
                }
                DarkEdifProperty prop = props.get(idx);
                if (prop.propTypeID != 23) { // PROPTYPE_IMAGELIST
                    throw new RuntimeException("Property " + prop.propName + " is not an image list.");
                }

                return prop.propData.getShort(0) & 0xFFFF;
            }

            public Point GetSizeProperty(Object chkIDOrName) {
                int idx = GetPropertyIndex(chkIDOrName);
                if (idx == -1) {
                    return null;
                }
                DarkEdifProperty prop = props.get(idx);
                if (prop.propTypeID != 8) { // PROPTYPE_SIZE
                    throw new RuntimeException("Property " + prop.propName + " is not an size property.");
                }

                Point size = new Point();
                size.x = prop.propData.getInt(0);
                size.y = prop.propData.getInt(4);
                return size;
            }

            @SuppressWarnings("UnusedReturnValue")
            public DarkEdifPropSetIterator LoopPropSet(String setName) {
                return LoopPropSet(setName, 0);
            }

            public DarkEdifPropSetIterator LoopPropSet(String setName, int numSkips) {
                DarkEdifProperty d;
                for (int i = 0, j = 0; i < numProps; ++i) {
                    d = props.get(i);
                    if (IsComboBoxProp((int)d.propTypeID) && (char)d.propData.get(0) == 'S') {
                        if (new DarkEdifPropSet(d.propData).setName.equals(setName) && ++j > numSkips) {
                            return new DarkEdifPropSetIterator(i, j - 1, d, this);
                        }
                    }
                }
                throw new RuntimeException("No set found with name " + setName + ".");
            }
        }

        private static final Map<String, Object> data = new HashMap<String, Object>();
        private static final int sdkVersion = 20;

        private DarkEdif() {
            throw new RuntimeException("DarkEdif is a static class, you cannot initialize it!");
        }

        public static Object getGlobalData(String key) {
            key = key.toLowerCase();
            if (data.containsKey(key)) {
                return data.get(key);
            }
            return null;
        }

        public static void setGlobalData(String key, Object value) {
            key = key.toLowerCase();
            data.put(key, value);
        }

        public int getCurrentFusionEventNumber(CRunExtension ext) {
            return ext.rh.rhEvtProg.rhEventGroup.evgIdentifier;
        }

        public static void checkSupportsSDKVersion(int sdkVer) {
            if (sdkVer < 16 || sdkVer > 20) {
                throw new RuntimeException("Flash DarkEdif SDK does not support SDK version " + sdkVersion);
            }
        }

        public static void consoleLog(CRunDarkEdif_Template ext, String str) {
            if (ext == null || ext.DebugMode) {
                final String extName = ext == null ? "Unknown DarkEdif ext" : ext.ExtensionName;
                Logger.getLogger(extName).log(Level.INFO, str);
            }
        }

        public static DarkEdifProperties getProperties(CRunExtension ext, CBinaryFile edPtrFile, int extVersion) {
            return new DarkEdifProperties(ext, edPtrFile, extVersion);
        }
    }
}
