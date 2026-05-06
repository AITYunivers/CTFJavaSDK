## Multimedia Java Runtime extension development kit

Welcome to the Java devkit for CTF. This kit will allow you to develop extension for the CTF Java runtime.

This repository is licensed under the Unlicense for code written by myself<br/>
Any code under StaticText and JavaRuntime, along with the RuntimeSDK.jar, is the copyright of Clickteam

### The Runtime

In Clickteam Fusion 2.5 Developer, the Java exporters are included.
Although they are hidden unless the "Show deprecated build types" checkbox
is checked.

### IDE

While the original SDK used a very old version of NetBeans, this
SDK is based on IntelliJ 2026.1.1. I'm certain you could also use
older or newer versions of IntelliJ without issue.

#### Projects

The extension devkit is contained in the JavaExtensions folder. You will
find three folders, and a Jar file there.

RuntimeSDK.jar : the runtime itself.

JavaRuntime : a simple project that launches the runtime.

Template : a template extension project, with empty functions.

StaticText : the StaticText extension as an example.

#### JavaRuntime.

This project should be loaded under IntelliJ. It is dynamically linked
to the \"RuntimeSDK.jar\" file. It only consists of a Main class, that
starts the runtime. The command line is important, and contains two
options :

`/f "C:\ExamplePath\Application.ccj" /e "C:\ExamplePath\CTFJavaSDK\Template"`

- /f : make this path point to the CCJ file you just saved.

- /e : points to the folder where the extension .class file is located.
You can point this to the root of the extension folder, as I do a
recursive search for the name of the .class file in the children
folders.

You can then append any command line arguments read by the application. 

### Creating you own new extension project

1. Copy the template folder, and rename it to the name of your extension

2. Under IntelliJ, load the new project

3. Right click on the project, and choose rename. This will change the name of the project.

4. IMPORTANT! In the Project Settings, under Artifacts, the name of the artifact MUST BE the exact same name as the extension mfx file (without the .mfx). Including upper and lower case. If you do not do that, the jarfile of your extension will NOT be recognised when you create a standalone java application.

5. Under src/, right click on the CRunTemplate file, choose Rename and enter the name of the new extension. The name of the extension MUST BE CRunNameOfExtension.java. \"NameOfExtension\" is the name of the C++ extension in the extensions folder (without the .mfx, and respecting the case). This is mandatory as I load the extension from the name of the class.

6. In the properties of the "Application" Run/Debug Configuration, you should insert the build/class folder of the extension you want to debug. Refer to [JavaRuntime](#javaruntime) for more info on arguments.

7. You can compile the extension to a .jar by double pressing the Shift key, then typing and selecting "Build Artifacts", you might also like to set a keybind for this. From there it will show a popup menu, just click Build or press enter. This will output the file to "out/artifacts/NameOfExtension/NameOfExtension.jar", if NameOfExtension is not set properly, please refer to Step 4.

#### How does development works?

Compile the extension, and put the .jar into "Data\Runtime\Java" in your Clickteam Fusion 2.5 folder, and then export your project as a Java Application.

Get the .ccj file of a project by extracting the .jar file exported by Clickteam Fusion 2.5 and it will be in the root folder, and then use that to follow Step 6.

From there, set a Breakpoint in CreateRunObject and run the "Application" Run/Debug Configuration in Debug mode. When the breakpoint is hit, at the top of the editor select "Choose Sources..." then browse and select your extension's project folder; 'CTFJavaSDK\NameOfExtension'.

Finally, stop the Application, then restart it. Now when your breakpoint is hit, the Choose Sources popup should be gone and the focused file should be the original file, rather than a decompilation.

**The extension class**

As stated before, the name of this class should be
\"CRunNameOfExtension\". It must NOT be located in a package, as loading
class files from packages does not work (well I could not make it work).

The class is derived from an abstract class, name CRunExtension.

The class contains functions similar to the C++ extensions, for the
runtime. Only more structured. Lets see the functions in details.

In CTF Java runtime, all object\'s name start with \"C\".

### Default variables

Two variables are defined in the abstract class parent from the object :

- ho : points to the CObject object. Equivalent to the headerObject structure in CTF. Ho is also useful as callback functions are defined to exchange data with the main runtime. I would ask you to prevent from addressing the fields of CObject directly to preserve compatibility with future versions. If the access to a field is not implemented, just ask me and I will program the functions.

- rh : points to the CRun object. Equivalent to the runHeader structure in CTF.

#### Constructor

Nothing special to do in the constructor of the extension. But you can
have your own code in it.

```java
public int getNumberOfConditions()
```

This function should return the number of conditions contained in the
object (equivalent to the CND_LAST define in the ext.h file).

```java
public boolean createRunObject(CBinaryFile file, CCreateObjectInfo
cob, int version)
```

This function is called when the object is created. As Java object are
just created when needed, there is no EDITDATA structure. Instead, I
send to the function a CBinaryFile object, pointing directly to the data
of the object (the EDITDATA structure, on disc).

The CBinaryFile object allows you to read the data. It automatically
converts PC-like ordering (big or little Indian I cant remember) into
Java ordering. It contains functions to read bytes, shorts, int, colors
and strings. Read the documentation about this class at the end of the
document.

\"Version\" contains the version value contained in the extHeader
structure of the EDITDATA.

So all you have to do, is read the data from the CBinaryFile object, and
initialise your object accordingly.

```java
public void destroyRunObject(boolean bFast)
```

Called when the object is destroyed. Due to garbage collection, this
routine should not have much to do, as all the data reserved in the
object will be freed at the next GC. bFast is true if the object is
destroyed at end of frame. It is false if the object is destroyed in the
middle of the application.

```java
public int handleRunObject()
```

Same as the C++ function. Perform all the tasks needed for your object
in this function. As the C function, this function returns value
indicating what to do :

- CRunExtension.REFLAG_ONESHOT : handleRunObject will not be called anymore

- CRunExtension.REFLAG_DISPLAY : displayRunObject is called at next refresh.

- Return 0 and the handleRunObject method will be called at the next loop.

```java
public void displayRunObject(Graphics2D g2)
```

Called to display the object. The parameter given is Graphics2D object
of the frame, where you must draw your object at the correct
co-ordinates.

For controls, this function is also called when you resize the window,
this time with a Graphics2D equal to null. When you resize the window, I
explore the list of object, and call the ones with a OEFLAG_WINDOWPROC
defined (this should include all the controls). You should reposition
the control at the correct position (using getXOffet and getYOffset).

```java
public void pauseRunObject()
```

Called when the application goes into pause mode.

```java
public void continueRunObject()
```

Called when the application restarts.

```java
public boolean saveRunObject(DataOutputStream stream)
```

Called when a Save Frame Position action is performed. This function
should save all the necessary data into the stream. If everything goes
fine, it should return true. False in case of error.

Note: load/save frame position do not work in this version.

```java
public boolean loadRunObject(DataInputStream stream)
```

This function is called when performing a Load Frame Position. It should
read all the data from the stream, and restore the object to its
original state. And return true if everything went OK.

```java
public void saveBackground(BufferedImage img)
```

If your extension saves the background itself, this function should do
the job. Upon entry you have a BufferedImage that contains the data to
save. You should create a BufferedImage where you save the content of
the background located at the co-ordinates of your object.

```java
public void restoreBackground(Graphics2D g)
```

Called when the background needs to be restored. You should draw the
content of your saved buffer in the Graphics2D you get upon entry.

```java
public void killBackground()
```

Proposed for compatibility with the C++ version. This function should
erase the save buffers.

```java
public CFontInfo getRunObjectFont()
```

Equivalent to the C++ version. This function returns a CFontInfo object,
a small object equivalent to the LOGFONT structure in C. See at the end
of the document the definition of the CFontInfo object.

```java
public void setRunObjectFont(CFontInfo fi, CRect rc)
```

Called when the font of the object needs to be changed. The rc parameter
is null when no resize is needed.

```java
public int getRunObjectTextColor()
```

Returns the current color of the text as an Integer.

```java
public void setRunObjectTextColor(int rgb)
```

Sets the current color of the text.

```java
public CMask getRunObjectCollisionMask(int flags)
```

If implemented, this function should return a CMask object that contains
the collision mask of the object. Return null in any other case.

```java
public BufferedImage getRunObjectSurface()
```

Called for backdrop objects prior to drawing the object, this function
should return a BufferedImage (with or without alpha channel) containing
the graphics to be drawn. The size of the image should be equivalent to
the width and height of the object. If not implemented, just returns
null.

```java
public void getZoneInfos()
```

This function should if necessary update the width and height values of
the object. Usually not needed.

```java
public boolean condition(int num, CCndExtension cnd)
```

The main entry for the evaluation of the conditions.

- num : number of the condition (equivalent to the CND\_ definitions in ext.h)

- cnd : a pointer to a CCndExtension object that contains useful callback functions to get the parameters.

- This function should return true or false, depending on the condition.

```java
public void action(int num, CActExtension act)
```

The main entry for the actions.

- num : number of the action, as defined in ext.h

- act : pointer to a CActExtension object that contains callback functions to get the parameters.

```java
public CValue expression(int num)
```

The main entry for expressions.

- num : number of the expression

To get the expression parameters, you have to call the getExpParam()
method defined in the \"ho\" variable, for each of the parameters. This
function returns a CValue which contains the parameter. You then do a
getInt(), getDouble() or getString() with the CValue object to grab the
actual value.

This method should return a CValue object containing the value to
return. The content of the CValue can be a integer, a double or a
String. There is no need to set the HOF_STRING flags if your return a
string : the CValue object contains the type of the returned value.

Callback functions

The \"ho\" variable in the extension object gives you access to the
CExtension object, which is a derivative of the main CObject class. I
send the object\'s source code in the devkit for your information, but
really I do not want to poke or peek in it, for compatibility reasons :
I want to be able to change the runtime when I want without changing the
extensions.

So I have programmed a few callback functions in the CExtension object.
Here are these functions :

```java
public JFrame getJFrame()
```

Returns the JFrame of the main CTF window.

```java
public JPanel getJPanel()
```

Returns the JPanel when all controls should be created. The Layout
manager of this JPanel is set to null, so you should use setBounds to
correctly place your control.

```java
public int getX()
```

Returns the current X co-ordinate of the object (hoX).

```java
public int getY()
```

Returns the current Y coordinate of the object (hoY)

```java
public int getWidth()
```

Returns the current width of the object (hoImgWidth).

```java
public int getHeight()
```

Returns the current height of the object (hoImgHeight).

```java
public void setPosition(int x, int y)
```

Changes the position of the object, taking the movement into account
(much better than poking into hoX and hoY).

```java
public void setX(int x)
```

Changes the position of the object (hoX), and takes care of the movement
and refresh. Same remark as setPosition.

```java
public void setY(int y)
```

Same as setX for Y co-ordinate. Same remark as setPosition.

```java
public void setWidth(int width)
```

Change the width of the object, taking care of the hoRect fields.

```java
public void setHeight(int height)
```

Same as setWidth, for height.

```java
public int scaleX(int x)
```

Calculates the resulting coordinate taking the strech to fill display
property. If the frame is not streched, this method has no effect, if it
has, it calculates the correct coordinate.

```java
public int scaleY(int y)
```

Same as before, for the Y coordinate.

```java
public Insets getInsets()
```

Returns the Insets object of the main frame. Maybe useful to position
your object. But for controls you would prefer getX/YOffset.

```java
public int getXOffset()
```

For controls only. Returns the X offset to where to position your object
in the JFrame. Takes insets into account and the position of the frame
in the window.

```java
public int getYOffset()
```

Same as getXOffset for Y co-ordinate.

```java
public void loadImageList(short\[\] imageList)
```

This method should be called in the createRunObject method of your
object. If your object uses images stored in the image bank, you must
call this method so that the proper images are loaded.

Just make an array with all the handles of the images, the size should
be the exact number of images to load. Call this method (it may take
some time to return). All the images will be loaded in the runtime.

```java
public CImage getImage(short handle)
```

Call this function to retrieve an image from a handle. The image must
have been previously loaded with loadImageList. The value returned could
be null if the image could not be loaded, but this is very unlikely to
occur.

CImage is part of the \"Banks\" package. So you should import it with
\"import Banks.\*\".

```java
public void reHandle()
```

If you returned a REFLAG_ONESHOT value in your handleRunObject method,
this will reinforce the method to be called at each loop.

```java
public void generateEvent(int code, int param)
```

Generate an event with the specific code. The parameter can be
recuperated with the ho.getEventParam method. You should prefer the
push_event method, specially if your event is generated in a listener
(MouseListener, EventListener) as the listener events occurs in another
thread and the event routines are not multi-thread proof.

```java
public void pushEvent(int code, int param)
```

This is the method of choice to generate an event.

```java
public void pause()
```

Pauses the application, when you have some lengthy work to perform (like
opening a file selector).

```java
public void resume()
```

Resumes the application at the end of your work.

```java
public void redisplay()
```

Redisplays the entire frame. This takes a long time (well sort of).

```java
public String getFileInfo(int code)
```

Get information about the pathnames of the current application. The code
indicates what you want to receive :

CRun.FILEINFO_DRIVE : returns the current drive

CRun.FILEINFO_DIR : returns the directory of the application.

CRun.FILEINFO_TEMPPATH :

CRun.FILEINFO_PATH : returns the complete path to the application.

CRun.FILEINFO_APPNAME : returns the name of the application.

```java
public void redraw()
```

Forces a redraw of the object (the displayRunObject routine is called at
next refresh).

```java
public void destroy()
```

Destroys the object at the end of the current loop.

```java
public void execProgram(String filename, String commandLine, short
flags)
```

Executes an external program.

- filename : the path to the file to execute.

- commandLine : the command line

- flags : PARAM_PROGRAM.PRGFLAGS_WAIT indicates that the application will wait for the end of the program to restart. PARAM_PROGRAM.PRGFLAGS_HIDE indicates that the main application window is hidden while the program is run. TO get these flags you should import Params.\*;

```java
public int getExtUserData()
```

Returns the private field of the extHeader structure.

```java
public void setExtUserData(int data)
```

Changes the private field of the extHeader structure.

```java
public int getEventCount()
```

Returns the rh4EventCount value, used in controls to trigger the events.

```java
public CValue getExpParam()
```

Returns the next expression parameter.

```java
public CObject getFirstObject()
```

Returns the first object currently defined in the frame. Should be used
in conjunction with getNextObject().

```java
public CObject getNextObject()
```

Returns the next object in the list of objects of the frame. Returns
null if no more objects is available. This method will return the
extension object it is called from.

#### CRun callback function

You access the CRun object via the \"rh\" variable defined in the
CRunExtension class. This object contains three methods used to define
global data.

Some extensions need to communicate between objects. In C++ this was
done simply by defining global variable in the code. This cannot be done
in Java, as each object is a separate entity. This is the reason why I
have defined three functions in Crun to allow you to create global
classes.

```java
public void addStorage(CExtStorage data, int id)
```

Adds a new object to the storage, with the \"id\" identifier. \"id\" is
a simple integer number. The storage class must be derived from the
class CExtStorage. This function has no effect if an object with the
same identifier already exists.

```java
public CExtStorage getStorage(int id)
```

Returns the storage object with the given identifier. Returns null if
the object is not found.

```java
public void delStorage(int id)
```

Deletes the object with the given identifier.

#### CActExtension callback methods

The CActExtension object is transmitted to the extension \"action\"
method when a action is called. This object contains callback function
to gather the parameters of the action.

These functions want two parameters:

- The CRun object (available in the extension as \"rh\").

- The number of the parameter in the action, starting at 0

```java
public CObject getParamObject(CRun rhPtr, int num)
```

Returns the CObject pointed to by the PARAM_OBJECT.

```java
public int getParamTime(CRun rhPtr, int num)
```

Returns the time value in milliseconds.

```java
public short getParamBorder(CRun rhPtr, int num)
```

Returns the border parameter.

```java
public short getParamDirection(CRun rhPtr, int num)
```

(obsolete, but might be used in old extensions). Returns a direction
from 0 to 31.

```java
public PARAM_CREATE getParamCreate(CRun rhPtr, int num)
```

Returns a pointer to the PARAM_CREATE object (for future use maybe).

```java
public int getParamAnimation(CRun rhPtr, int num)
```

Returns the number of the animation.

```java
public short getParamPlayer(CRun rhPtr, int num)
```

Returns the number of the player.

```java
public PARAM_EVERY getParamEvery(CRun rhPtr, int num)
```

Returns a pointer to the Every parameter.

```java
public short getParamKey(CRun rhPtr, int num)
```

Return the key code contained in the parameter.

```java
public int getParamSpeed(CRun rhPtr, int num)
```

Returns a speed, from 0 to 100.

```java
public CpositionInfo getParamPosition(CRun rhPtr, int num)
```

Returns a pointer to a CpositionInfo classe that contains the X and Y
coordinate. CPositionInfo is defined in the Params package.

```java
public short getParamJoyDirection(CRun rhPtr, int num)
```

Returns a joystick direction.

```java
public PARAM_SHOOT getParamShoot(CRun rhPtr, int num)
```

Returns a pointer to the PARAM_SHOOT object contained in the action (for
future use maybe).

```java
public PARAM_ZONE getParamZone(CRun rhPtr, int num)
```

Returns a pointer to the PARAM_ZONE parameter.

```java
public int getParamExpression(CRun rhPtr, int num)
```

Returns the value contained in the expression.

```java
public int getParamColour(CRun rhPtr, int num)
```

Returns a color, as an integer.

```java
public short getParamFrame(CRun rhPtr, int num)
```

Returns a number of frame.

```java
public int getParamNewDirection(CRun rhPtr, int num)
```

Returns a direction, from 0 to 31.

```java
public short getParamClick(CRun rhPtr, int num)
```

Returns the click parameter (left/middle/right button).

```java
public PARAM_PROGRAM getParamProgram(CRun rhPtr, int num)
```

Returns the PARAM_PROGRAM object to launch an external program.

```java
public String getParamFilename(CRun rhPtr, int num)
```

Returns a filename as a string.

```java
public String getParamExpString(CRun rhPtr, int num)
```

Returns the result of a String expression.

```java
public double getParamExpDouble(CRun rhPtr, int num)
```

Returns as a double the result of the evaluation of the parameter.

```java
public String getParamFilename2(CRun rhPtr, int num)
```

Returns the filename as a String object.

#### CCndExtension callback functions

The CCndExtension object is transmitted to the extension when calling
the condition method. It contains callbacks to gather the parameters of
the condition. Most of the method are identical to the ones in
CActExtension, with the following differences :

##### Missing methods:

getParamPosition

getParamCreate

getParamShoot

##### Different returns

The getParamObject method returns a pointer to the PARAM_OBJECT object
(as in the C++).

`public PARAM_OBJECT getParamObject(CRun rhPtr, int num)`

```java
public boolean compareValues(CRun rhPtr, int num, CValue value)
```

In the C++ version, when you had a PARAM_COMPARAISON parameter in the
condition, the condition routine returned a long value, and CTF was
automatically doing the comparison with the parameter. You have to call
this method in the Java version. For example, a condition with a
PARAM_COMPARAISON as first parameter, the end of the condition method
should be:

`return cnd.compareValues(rh, 0, returnValue);`

Where returnValue is a CValue object containing the value to compare
with.

```java
public boolean compareTime(CRun rhPtr, int num, int t)
```

Same as the previous function, for PARAM_CMPTIME parameters, where \"t\"
is the time to compare to.

#### Useful objects

I will document now some of the objects of the runtime you will be using
to program your extension. You can find the source code of these objects
in the \"Object\" folder of the devkit.

##### <u>The CBinaryFile object</u>

This object is sent to you in the createRunObject method. It
automatically points to the start of the extension data (EDITDATA
structure, in memory). It automatically performs the indian translation
between PC values and Java values.

```java
public void read(byte\[\] b)
```

Read a array of bytes. The number of bytes read is equivalent to the
size of the array transmitted as a parameter.

```java
public void skipBytes(int n)
```

Skips bytes in the file.

```java
public int getFilePointer()
```

Returns the current file pointer position.

```java
public void seek(int pos)
```

Change the file pointer position.

```java
public byte readByte()
```
Reads one byte.

```java
public short readShort()
```

Reads one short (two bytes).

```java
public int readInt()
```

Reads an integer (4 bytes).

```java
public int readColor()
```

Reads a color making it compatible with Java color values (inversion of
Red and Blue values).

```java
public String readString(int size)
```

Reads a string of the given size (if the string does not finish by 0.)

```java
public String readString()
```

Reads a string that finishes with 0.

```java
public CFontInfo readLogFont()
```

Reads a LOGFONT structure into a CFontInfo object.

```java
public void skipString()
```

Skips a string ending with 0.

##### <u>The CValue object</u>

The CValue object is used in expression evaluation. It can contain an
Integer, a double or a String.

```java
public CValue()
```

The default constructor : create the object with type integer, value 0.

```java
public CValue(CValue value)
```

Creates the object by grabbing the value and type from the given CValue.

```java
public CValue(int i)
```

Creates the object as type integer with the given value.

```java
public CValue(double d)
```

Creates the object as type double, with the given value.

```java
public CValue(String s)
```

Creates the object as type String, with the given value.

```java
public byte getType()
```

Returns the type of the object. The return value can be :

CValue.TYPE_INT : and integer

CValue.TYPE_DOUBLE : a double

CValue.TYPE_STRING : a string

```java
public int getInt()
```

Returns an integer. (if the object is of TYPE_DOUBLE, converts the value
to int).

```java
public double getDouble()
```

Returns a double.

```java
public String getString()
```

Returns the string. Warning, if the object is of type int or double,
this will NOT convert the value to string.

```java
public void forceInt(int value)
```

Forces the value of type int into the object (if the object was
previously double or int, then changes the type).

```java
public void forceDouble(double value)
```

Forces the value of type double.

```java
public void forceString(String value)
```

Forces a string.

```java
public void forceValue(CValue value)
```

Forces the content and type of the CValue.

```java
public void setValue(CValue value)
```

Change the content of the object, respecting its type.

##### <u>The CFontInfo object</u>

The CFontInfo object is a replacement of the LOGFONT structure in C++.
It contains the name of the font, its height, weight and attributes.

**Fields contained in the object :**

```java
public int lfHeight=0;

public int lfWeight=0;

public byte lfItalic=0;

public byte lfUnderline=0;

public byte lfStrikeOut=0;

public String lfFaceName=null;
```

```java
public Font createFont()
```

Creates a Font object based on the content of the object.

Note: you can read the content of a LOGFONT from a CBinaryFile object
with the readLogFont method.

```java
public void write(DataOutputStream stream) throws IOException
```

Writes the content of the object into a DataOutputStream. Use this
function in the saveRunObject method.

```java
public void read(DataInputStream stream) throws IOException
```

Reads the content of the object from a DataInputStream.

##### <u>The CRect object</u>

This object is intended as a replacement of the C++ Rect structure.

**Fields :**
```java
public int left=0;

public int top=0;

public int right=0;

public int bottom=0;
```
```java
public void copyRect(CRect srce)
```

Copies the content of the gievn CRect object in the object.

```java
public void write(DataOutputStream s) throws IOException
```

Write the content of the object into a DataOutputStream.

```java
public void read(DataInputStream s) throws IOException
```

Reads the content of the object from a DataInputStream.

```java
public boolean ptInRect(int x, int y)
```

Returns true if the given point is located in the rectangle.

```java
public boolean intersectRect(CRect rc)
```

Returns true if the two rectangle intersect.

##### <u>The CPoint object</u>

The CPoint object is a replacement of the C++ POINT structure. Is only
contains two fields:
```java
public int x=0;

public int y=0;
```
