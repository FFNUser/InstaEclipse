package ps.reso.instaeclipse.mods;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ps.reso.instaeclipse.utils.Utils;

public class DevOptionsEnable {
    public void handleDevOptions(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            ClassLoader classLoader = lpparam.classLoader;
            handleAutoMode(lpparam);
        } catch (Exception e) {
            XposedBridge.log("(DevOptionsEnable) Error handling Dev Options: " + e.getMessage());
        }
    }
    private void handleAutoMode(XC_LoadPackage.LoadPackageParam lpparam) {
        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        try {
            int num_of_hooks = 0;
            //  Perform dynamic search
            for (char first : characters.toCharArray()) {
                for (char second : characters.toCharArray()) {
                    for (char third : characters.toCharArray()) {
                        String classToHook = "X." + first + second + third;

                        try {
                            Class<?> cls = XposedHelpers.findClass(classToHook, lpparam.classLoader);

                            Method[] methods = cls.getDeclaredMethods();
                                try {
                                    if (methods.length == 1 && methods[0].getName().equals("A00") &&
                                            methods[0].getReturnType() == Boolean.TYPE &&
                                            methods[0].getParameterCount() == 1) {

                                        Class<?> declaringClass = methods[0].getDeclaringClass();

                                        if (declaringClass != null) {
                                            Field[] fields = declaringClass.getDeclaredFields();
                                            if (fields.length == 0) {

                                                num_of_hooks += 1;
                                                Class<?> UserSessionClass = XposedHelpers.findClass(Utils.USER_SESSION_CLASS, lpparam.classLoader);
                                                hookDevOptions(cls, "A00", UserSessionClass);

                                            }

                                        }
                                    }
                                } catch (NoClassDefFoundError | XposedHelpers.ClassNotFoundError e) {
                                    //XposedBridge.log("(DevOptionsEnable) Skipping method due to missing dependency: " + e.getMessage());
                                } catch (Exception e) {
                                    //XposedBridge.log("(DevOptionsEnable) General exception while inspecting method: " + e.getMessage());
                                }

                        } catch (NoClassDefFoundError | XposedHelpers.ClassNotFoundError e) {
                            //XposedBridge.log("(DevOptionsEnable) Skipping class " + classToHook + " due to missing dependency: " + e.getMessage());
                        } catch (Exception e) {
                            //XposedBridge.log("(DevOptionsEnable) General exception while inspecting class: " + e.getMessage());
                        }
                    }
                }
            }

            if (num_of_hooks <= 0){
                // No suitable classes found
                XposedBridge.log("(DevOptionsEnable) No suitable classes found during dynamic search.");
            }

        } catch (Exception e) {
            XposedBridge.log("(DevOptionsEnable) Error in Auto mode: " + e.getMessage());
        }
    }


    private void hookDevOptions(Class<?> targetClass, String methodToHook, Class<?> secondTargetClass) {
        try {
            XposedHelpers.findAndHookMethod(
                    targetClass,
                    methodToHook,
                    secondTargetClass, // Second class parameter (UserSessionClass)
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) {
                            XposedBridge.log("(DevOptionsEnable) Successfully Hooked into method: " + methodToHook + " in class: " + targetClass.getName());
                            return true; // Ensure the method always returns true
                        }
                    }
            );
            //XposedBridge.log("(DevOptionsEnable) Successfully hooked method: " + methodToHook + " in class: " + targetClass.getName());
        } catch (NoSuchMethodError e) {
            //XposedBridge.log("(DevOptionsEnable) No such method: " + methodToHook + " in class: " + targetClass.getName() + " - " + e.getMessage());
        } catch (NoClassDefFoundError e) {
            //XposedBridge.log("(DevOptionsEnable) No such class definition found for: " + targetClass.getName() + " or parameter class: " + secondTargetClass.getName() + " - " + e.getMessage());
        } catch (XposedHelpers.ClassNotFoundError e) {
            //XposedBridge.log("(DevOptionsEnable) XposedHelpers couldn't find class: " + targetClass.getName() + " or parameter class: " + secondTargetClass.getName() + " - " + e.getMessage());
        } catch (Exception e) {
            //XposedBridge.log("(DevOptionsEnable) General exception while hooking method: " + methodToHook + " in class: " + targetClass.getName() + " - " + e.getMessage());
        }
    }
}