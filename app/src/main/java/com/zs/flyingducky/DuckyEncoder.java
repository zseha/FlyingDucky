package com.zs.flyingducky;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Originaly written by hak5darren for Rubber Ducky
 *       _      _      _      USB       _      _      _
 __(.)< __(.)> __(.)=   Rubber   >(.)__ <(.)__ =(.)__
 \___)  \___)  \___)    Ducky!    (___/  (___/  (___/
 The USB Rubber Ducky is a Human Interface Device programmable with a simple scripting language
 allowing penetration testers to quickly and easily craft and deploy security auditing payloads that
 mimic human keyboard input. The source is written in C and requires the AVR Studio 5 IDE
 from atmel.com/avrstudio.
 Hardware is commercially available at hakshop.com.
 Tools and payloads can be found at usbrubberducky.com. Quack!
 */

public class DuckyEncoder {

    private static Properties keyboardProps = new Properties();
    private static Properties layoutProps = new Properties();


    private static void encodeToFile(String inStr, String fileDest) {

        inStr = inStr.replaceAll("\\r", ""); // CRLF Fix
        String[] instructions = inStr.split("\n");
        String[] last_instruction = inStr.split("\n");
        List<Byte> file = new ArrayList<Byte>();
        int defaultDelay = 0;
        int loop =0;
        boolean repeat=false;
        System.out.println("Loading DuckyScript .....\t[ OK ]");
        //if(debug) System.out.println("\nParsing Commands:");
        for (int i = 0; i < instructions.length; i++) {
            try {
                boolean delayOverride = false;
                String commentCheck = instructions[i].substring(0, 2);
                if (commentCheck.equals("//"))
                    continue;
                if (instructions[i].equals("\n"))
                    continue;
                String[] instruction = instructions[i].split(" ", 2);

                if(i>0){
                    last_instruction=instructions[i-1].split(" ", 2);
                    last_instruction[0].trim();
                    if (last_instruction.length == 2) {
                        last_instruction[1].trim();
                    }
                }else{
                    last_instruction=instructions[i].split(" ", 2);
                    last_instruction[0].trim();
                    if (last_instruction.length == 2) {
                        last_instruction[1].trim();
                    }
                }

                instruction[0].trim();

                if (instruction.length == 2) {
                    instruction[1].trim();
                }

                if (instruction[0].equals("REM")){
                    continue;
                }
                if (instruction[0].equals("REPEAT")){
                    loop=Integer.parseInt(instruction[1].trim());
                    repeat=true;
                }else{
                    repeat=false;
                    loop=1;
                }
                while(loop>0){
                    if (repeat){
                        instruction=last_instruction;
                        //System.out.println(Integer.toString(instruction.length));
                    }
                    //if (debug) System.out.println(java.util.Arrays.toString(instruction));
                    if (instruction[0].equals("DEFAULT_DELAY")
                            || instruction[0].equals("DEFAULTDELAY")) {
                        defaultDelay = Integer.parseInt(instruction[1].trim());
                        delayOverride = true;
                    } else if (instruction[0].equals("DELAY")) {
                        int delay = Integer.parseInt(instruction[1].trim());
                        while (delay > 0) {
                            file.add((byte) 0x00);
                            if (delay > 255) {
                                file.add((byte) 0xFF);
                                delay = delay - 255;
                            } else {
                                file.add((byte) delay);
                                delay = 0;
                            }
                        }
                        delayOverride = true;
                    } else if (instruction[0].equals("STRING")) {
                        for (int j = 0; j < instruction[1].length(); j++) {
                            char c = instruction[1].charAt(j);
                            addBytes(file,charToBytes(c));
                        }
                    } else if (instruction[0].equals("STRING_DELAY")) {
                        final String[] twoOptions = instruction[1].split(" ", 2);
                        final int delayMillis = Integer.parseInt(twoOptions[0].trim());
                        final String userText = twoOptions[1].trim();

                        //if(debug)  System.out.println(delayMillis);
                        //if(debug)  System.out.println(userText);

                        for (int j = 0; j < userText.length(); j++) {
                            char c = userText.charAt(j);
                            addBytes(file,charToBytes(c));

                            // Now insert the delay before the next character (and after the last is provided)
                            for(int counter = delayMillis; counter > 0; counter -= 0xFF) {
                                file.add((byte) 0x00);
                                if(counter > 0xFF) {
                                    file.add((byte) 0xFF);
                                } else {
                                    file.add((byte) counter);  // Last one
                                }
                            }
                        }
                    } else if (instruction[0].equals("CONTROL")
                            || instruction[0].equals("CTRL")) {
                        if (instruction.length != 1){
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_CTRL")));
                        } else {
                            file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_CTRL")));
                            file.add((byte) 0x00);
                        }
                    } else if (instruction[0].equals("ALT")) {
                        if (instruction.length != 1){
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_ALT")));
                        } else {
                            file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_ALT")));
                            file.add((byte) 0x00);
                        }
                    } else if (instruction[0].equals("SHIFT")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT")));
                        } else {
                            file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_SHIFT")));
                            file.add((byte) 0x00);
                        }
                    } else if (instruction[0].equals("CTRL-ALT")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_CTRL"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_ALT"))));
                        } else {
                            continue;
                        }
                    } else if (instruction[0].equals("CTRL-SHIFT")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_CTRL"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT"))));
                        } else {
                            continue;
                        }
                    } else if (instruction[0].equals("COMMAND-OPTION")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_KEY_LEFT_GUI"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_ALT"))));
                        } else {
                            continue;
                        }
                    } else if (instruction[0].equals("ALT-SHIFT")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_ALT"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT")))
                            );
                        } else {
                            file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_ALT")));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_ALT"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT")))
                            );
                        }
                    } else if (instruction[0].equals("ALT-TAB")){
                        if (instruction.length == 1) {
                            file.add(strToByte(keyboardProps.getProperty("KEY_TAB")));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_ALT")));
                        } else{
                            // do something?
                        }
                    } else if (instruction[0].equals("REM")) {
                                        /* no default delay for the comments */
                        delayOverride = true;
                        continue;
                    } else if (instruction[0].equals("WINDOWS")
                            || instruction[0].equals("GUI")) {
                        if (instruction.length == 1) {
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_GUI")));
                            file.add((byte) 0x00);
                        } else {
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_GUI")));
                        }
                    } else if (instruction[0].equals("COMMAND")){
                        if (instruction.length == 1) {
                            file.add(strToByte(keyboardProps.getProperty("KEY_COMMAND")));
                            file.add((byte) 0x00);
                        } else {
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_GUI")));
                        }
                    }else {
                                        /* treat anything else as a key */
                        file.add(strInstrToByte(instruction[0]));
                        file.add((byte) 0x00);
                    }
                    loop--;
                }
                // Default delay
                if (!delayOverride & defaultDelay > 0) {
                    int delayCounter = defaultDelay;
                    while (delayCounter > 0) {
                        file.add((byte) 0x00);
                        if (delayCounter > 255) {
                            file.add((byte) 0xFF);
                            delayCounter = delayCounter - 255;
                        } else {
                            file.add((byte) delayCounter);
                            delayCounter = 0;
                        }
                    }
                }
            }catch (StringIndexOutOfBoundsException e){
                //do nothing
            }
            catch (Exception e) {
                System.out.println("Error on Line: " + (i + 1));
                e.printStackTrace();
            }
        }

        // Write byte array to file
        byte[] data = new byte[file.size()];
        for (int i = 0; i < file.size(); i++) {
            data[i] = file.get(i);
        }
        try {
            File someFile = new File(fileDest);
            FileOutputStream fos = new FileOutputStream(someFile);
            fos.write(data);
            fos.flush();
            fos.close();
            System.out.println("DuckyScript Complete.....\t[ OK ]\n");
        } catch (Exception e) {
            System.out.print("Failed to write hex file!");
        }

    }

    private static void addBytes(List<Byte> file, byte[] byteTab){
        for(int i=0;i<byteTab.length;i++)
            file.add(byteTab[i]);
        if(byteTab.length % 2 != 0){
            file.add((byte) 0x00);
        }
    }
    private static byte[] charToBytes (char c){
        return codeToBytes(charToCode(c));
    }
    private static String charToCode (char c){
        String code;
        if(c<128){
            code = "ASCII_"+Integer.toHexString(c).toUpperCase();
        }else if (c<256){
            code = "ISO_8859_1_"+Integer.toHexString(c).toUpperCase();
        }else{
            code = "UNICODE_"+Integer.toHexString(c).toUpperCase();
        }
        return code;
    }
    private static byte[] codeToBytes (String str){
        if(layoutProps.getProperty(str) != null){
            String keys[] = layoutProps.getProperty(str).split(",");
            byte[] byteTab = new byte[keys.length];
            for(int j=0;j<keys.length;j++){
                String key = keys[j].trim();
                if(keyboardProps.getProperty(key) != null){
                    byteTab[j] = strToByte(keyboardProps.getProperty(key).trim());
                }else if(layoutProps.getProperty(key) != null){
                    byteTab[j] = strToByte(layoutProps.getProperty(key).trim());
                }else{
                    System.out.println("Key not found:"+key);
                    byteTab[j] = (byte) 0x00;
                }
            }
            return byteTab;
        }else{
            System.out.println("Char not found:"+str);
            byte[] byteTab = new byte[1];
            byteTab[0] = (byte) 0x00;
            return byteTab;
        }
    }
    private static byte strToByte(String str) {
        if(str.startsWith("0x")){
            return (byte)Integer.parseInt(str.substring(2),16);
        }else{
            return (byte)Integer.parseInt(str);
        }
    }
    private static byte strInstrToByte(String instruction){
        instruction = instruction.trim();
        if(keyboardProps.getProperty("KEY_"+instruction)!=null)
            return strToByte(keyboardProps.getProperty("KEY_"+instruction));
                /* instruction different from the key name */
        if(instruction.equals("ESCAPE"))
            return strInstrToByte("ESC");
        if(instruction.equals("DEL"))
            return strInstrToByte("DELETE");
        if(instruction.equals("BREAK"))
            return strInstrToByte("PAUSE");
        if(instruction.equals("CONTROL"))
            return strInstrToByte("CTRL");
        if(instruction.equals("DOWNARROW"))
            return strInstrToByte("DOWN");
        if(instruction.equals("UPARROW"))
            return strInstrToByte("UP");
        if(instruction.equals("LEFTARROW"))
            return strInstrToByte("LEFT");
        if(instruction.equals("RIGHTARROW"))
            return strInstrToByte("RIGHT");
        if(instruction.equals("MENU"))
            return strInstrToByte("APP");
        if(instruction.equals("WINDOWS"))
            return strInstrToByte("GUI");
        if(instruction.equals("PLAY") || instruction.equals("PAUSE"))
            return strInstrToByte("MEDIA_PLAY_PAUSE");
        if(instruction.equals("STOP"))
            return strInstrToByte("MEDIA_STOP");
        if(instruction.equals("MUTE"))
            return strInstrToByte("MEDIA_MUTE");
        if(instruction.equals("VOLUMEUP"))
            return strInstrToByte("MEDIA_VOLUME_INC");
        if(instruction.equals("VOLUMEDOWN"))
            return strInstrToByte("MEDIA_VOLUME_DEC");
        if(instruction.equals("SCROLLLOCK"))
            return strInstrToByte("SCROLL_LOCK");
        if(instruction.equals("NUMLOCK"))
            return strInstrToByte("NUM_LOCK");
        if(instruction.equals("CAPSLOCK"))
            return strInstrToByte("CAPS_LOCK");
                /* else take first letter */
        return charToBytes(instruction.charAt(0))[0];
    }
    private static Byte[] encodeToByteArray(String inStr) {
        inStr = inStr.replaceAll("\\r", ""); // CRLF Fix
        String[] instructions = inStr.split("\n");
        String[] last_instruction = inStr.split("\n");
        List<Byte> file = new ArrayList<Byte>();
        int defaultDelay = 0;
        int loop = 0;
        boolean repeat = false;
        /**
         *  TODO:
         *  add logging
         */


        //System.out.println("Loading DuckyScript .....\t[ OK ]");
        //if(debug) System.out.println("\nParsing Commands:");
        for (int i = 0; i < instructions.length; i++) {
            try {
                boolean delayOverride = false;
                String commentCheck = instructions[i].substring(0, 2);
                if (commentCheck.equals("//"))
                    continue;
                if (instructions[i].equals("\n"))
                    continue;
                String[] instruction = instructions[i].split(" ", 2);

                if (i > 0) {
                    last_instruction = instructions[i - 1].split(" ", 2);
                    last_instruction[0].trim();
                    if (last_instruction.length == 2) {
                        last_instruction[1].trim();
                    }
                } else {
                    last_instruction = instructions[i].split(" ", 2);
                    last_instruction[0].trim();
                    if (last_instruction.length == 2) {
                        last_instruction[1].trim();
                    }
                }

                instruction[0].trim();

                if (instruction.length == 2) {
                    instruction[1].trim();
                }

                if (instruction[0].equals("REM")) {
                    continue;
                }
                if (instruction[0].equals("REPEAT")) {
                    loop = Integer.parseInt(instruction[1].trim());
                    repeat = true;
                } else {
                    repeat = false;
                    loop = 1;
                }
                while (loop > 0) {
                    if (repeat) {
                        instruction = last_instruction;
                        //System.out.println(Integer.toString(instruction.length));
                    }
                    //if (debug) System.out.println(java.util.Arrays.toString(instruction));
                    if (instruction[0].equals("DEFAULT_DELAY")
                            || instruction[0].equals("DEFAULTDELAY")) {
                        defaultDelay = Integer.parseInt(instruction[1].trim());
                        delayOverride = true;
                    } else if (instruction[0].equals("DELAY")) {
                        int delay = Integer.parseInt(instruction[1].trim());
                        while (delay > 0) {
                            file.add((byte) 0x00);
                            if (delay > 255) {
                                file.add((byte) 0xFF);
                                delay = delay - 255;
                            } else {
                                file.add((byte) delay);
                                delay = 0;
                            }
                        }
                        delayOverride = true;
                    } else if (instruction[0].equals("STRING")) {
                        for (int j = 0; j < instruction[1].length(); j++) {
                            char c = instruction[1].charAt(j);
                            addBytes(file, charToBytes(c));
                        }
                    } else if (instruction[0].equals("STRING_DELAY")) {
                        final String[] twoOptions = instruction[1].split(" ", 2);
                        final int delayMillis = Integer.parseInt(twoOptions[0].trim());
                        final String userText = twoOptions[1].trim();

                        //if(debug)  System.out.println(delayMillis);
                        //if(debug)  System.out.println(userText);

                        for (int j = 0; j < userText.length(); j++) {
                            char c = userText.charAt(j);
                            addBytes(file, charToBytes(c));

                            // Now insert the delay before the next character (and after the last is provided)
                            for (int counter = delayMillis; counter > 0; counter -= 0xFF) {
                                file.add((byte) 0x00);
                                if (counter > 0xFF) {
                                    file.add((byte) 0xFF);
                                } else {
                                    file.add((byte) counter);  // Last one
                                }
                            }
                        }
                    } else if (instruction[0].equals("CONTROL")
                            || instruction[0].equals("CTRL")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_CTRL")));
                        } else {
                            file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_CTRL")));
                            file.add((byte) 0x00);
                        }
                    } else if (instruction[0].equals("ALT")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_ALT")));
                        } else {
                            file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_ALT")));
                            file.add((byte) 0x00);
                        }
                    } else if (instruction[0].equals("SHIFT")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT")));
                        } else {
                            file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_SHIFT")));
                            file.add((byte) 0x00);
                        }
                    } else if (instruction[0].equals("CTRL-ALT")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_CTRL"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_ALT"))));
                        } else {
                            continue;
                        }
                    } else if (instruction[0].equals("CTRL-SHIFT")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_CTRL"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT"))));
                        } else {
                            continue;
                        }
                    } else if (instruction[0].equals("COMMAND-OPTION")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_KEY_LEFT_GUI"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_ALT"))));
                        } else {
                            continue;
                        }
                    } else if (instruction[0].equals("ALT-SHIFT")) {
                        if (instruction.length != 1) {
                            file.add(strInstrToByte(instruction[1]));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_ALT"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT")))
                            );
                        } else {
                            file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_ALT")));
                            file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_ALT"))
                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT")))
                            );
                        }
                    } else if (instruction[0].equals("ALT-TAB")) {
                        if (instruction.length == 1) {
                            file.add(strToByte(keyboardProps.getProperty("KEY_TAB")));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_ALT")));
                        } else {
                            // do something?
                        }
                    } else if (instruction[0].equals("REM")) {
                                        /* no default delay for the comments */
                        delayOverride = true;
                        continue;
                    } else if (instruction[0].equals("WINDOWS")
                            || instruction[0].equals("GUI")) {
                        if (instruction.length == 1) {
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_GUI")));
                            file.add((byte) 0x00);
                        } else {
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_GUI")));
                        }
                    } else if (instruction[0].equals("COMMAND")) {
                        if (instruction.length == 1) {
                            file.add(strToByte(keyboardProps.getProperty("KEY_COMMAND")));
                            file.add((byte) 0x00);
                        } else {
                            file.add(strInstrToByte(instruction[1]));
                            file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_GUI")));
                        }
                    } else {
                                        /* treat anything else as a key */
                        file.add(strInstrToByte(instruction[0]));
                        file.add((byte) 0x00);
                    }
                    loop--;
                }
                // Default delay
                if (!delayOverride & defaultDelay > 0) {
                    int delayCounter = defaultDelay;
                    while (delayCounter > 0) {
                        file.add((byte) 0x00);
                        if (delayCounter > 255) {
                            file.add((byte) 0xFF);
                            delayCounter = delayCounter - 255;
                        } else {
                            file.add((byte) delayCounter);
                            delayCounter = 0;
                        }
                    }
                }
            } catch (StringIndexOutOfBoundsException e) {
                //do nothing
            } catch (Exception e) {
                System.out.println("Error on Line: " + (i + 1));
                e.printStackTrace();
            }
        }
        return file.toArray(new Byte[0]);
    }
}




