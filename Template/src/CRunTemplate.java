//----------------------------------------------------------------------------------
//
// CRUNTEMPLATE: Template extension object by Yunivers
//
//----------------------------------------------------------------------------------

import Actions.CActExtension;
import Conditions.CCndExtension;
import Expressions.CValue;
import Extensions.CRunExtension;
import RunLoop.CCreateObjectInfo;
import Services.CBinaryFile;

import java.awt.*;

public class CRunTemplate extends CRunExtension {
    public CRunTemplate() {

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
    @Override
    public void action(int num, CActExtension act) {
        // The main entry for the actions.
        // - num : number of the action, as defined in ext.h
        // - act : pointer to a CActExtension object that contains callback
        // functions to get the parameters.
    }

    // Expressions
    // --------------------------------------------
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
	    return null;
    }
}
