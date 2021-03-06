/*
 * Copyright (c) 2017-2019 superblaubeere27, Sam Sun, MarcoMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.superblaubeere27.jobf.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.superblaubeere27.jobf.JObfImpl;
import me.superblaubeere27.jobf.processors.name.ClassWrapper;
import me.superblaubeere27.jobf.utils.values.DeprecationLevel;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Random;
import java.util.StringJoiner;
import java.util.zip.ZipFile;

import static org.objectweb.asm.Opcodes.*;

public class Utils {
    private static final Random random = new Random();

    public static ClassNode lookupClass(String name) {
        ClassWrapper a = JObfImpl.INSTANCE.getClassPath().get(name);

        if (a != null) return a.classNode;

        return JObfImpl.getClasses().get(name);
    }


    public static MethodNode getMethod(ClassNode cls, String name, String desc) {
        for (MethodNode method : cls.methods) {
            if (method.name.equals(name) && method.desc.equals(desc))
                return method;
        }
        return null;
    }

    public static FieldNode getField(ClassNode cls, String name) {
        for (FieldNode method : cls.fields) {
            if (method.name.equals(name))
                return method;
        }
        return null;
    }

    private static boolean isNotInstruction(AbstractInsnNode node) {
        return node instanceof LineNumberNode || node instanceof FrameNode || node instanceof LabelNode;
    }

    public static boolean notAbstractOrNative(MethodNode methodNode) {
        return !Modifier.isNative(methodNode.access) && !Modifier.isAbstract(methodNode.access);
    }

    public static AbstractInsnNode getNextFollowGoto(AbstractInsnNode node) {
        AbstractInsnNode next = node.getNext();
        while (next instanceof LabelNode || next instanceof LineNumberNode || next instanceof FrameNode) {
            next = next.getNext();
        }
        if (next.getOpcode() == Opcodes.GOTO) {
            JumpInsnNode cast = (JumpInsnNode) next;
            next = cast.label;
            while (Utils.isNotInstruction(next)) {
                next = next.getNext();
            }
        }
        return next;
    }

    public static AbstractInsnNode getNext(AbstractInsnNode node) {
        if (node == null) return null;
        AbstractInsnNode next = node.getNext();

        if (next == null) return null;

        while (Utils.isNotInstruction(next)) {
            next = next.getNext();

            if (next == null) break;
        }
        return next;
    }

    public static AbstractInsnNode getPrevious(AbstractInsnNode node, int amount) {
        for (int i = 0; i < amount; i++) {
            node = getPrevious(node);
        }
        return node;
    }

    public static AbstractInsnNode getPrevious(AbstractInsnNode node) {
        AbstractInsnNode prev = node.getPrevious();
        while (Utils.isNotInstruction(prev)) {
            prev = prev.getPrevious();
        }
        return prev;
    }

    public static int random(int min, int max) {
        return min >= max ? min : random.nextInt(max - min) + min;
    }

    public static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public static HashMap<LabelNode, LabelNode> generateNewLabelMap(InsnList insnList) {
        HashMap<LabelNode, LabelNode> labelNodeHashMap = new HashMap<>();

        for (AbstractInsnNode abstractInsnNode : insnList.toArray()) {
            if (abstractInsnNode instanceof LabelNode) {
                LabelNode label = (LabelNode) abstractInsnNode;

                labelNodeHashMap.put(label, new LabelNode());
            }
        }

        return labelNodeHashMap;
    }

    public static boolean matchMethodNode(MethodInsnNode methodInsnNode, String s) {
        return s.equals(methodInsnNode.owner + "." + methodInsnNode.name + ":" + methodInsnNode.desc);
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String chooseDirectory(final File currFolder, final Component parent) {
        final JFileChooser chooser = new JFileChooser(currFolder);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile().getAbsolutePath();
        return null;
    }

    public static String chooseDirectoryOrFile(final File currFolder, final Component parent) {
        final JFileChooser chooser = new JFileChooser(currFolder);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile().getAbsolutePath();
        return null;
    }

    public static String chooseFile(final File currFolder, final Component parent) {
        final JFileChooser chooser = new JFileChooser(currFolder);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile().getAbsolutePath();
        return null;
    }

    public static String chooseFile(final File currFolder, final Component parent, FileFilter filter) {
        final JFileChooser chooser = new JFileChooser(currFolder);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile().getAbsolutePath();
        return null;
    }

    public static String chooseFileToSave(final File currFolder, final Component parent, FileFilter filter) {
        final JFileChooser chooser = new JFileChooser(currFolder);
        chooser.setFileFilter(filter);
        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile().getAbsolutePath();
        return null;
    }

    public static long copy(final InputStream from, final OutputStream to) throws IOException {
        byte[] buf = new byte[1024];
        long total = 0L;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    public static Color getColor(DeprecationLevel deprecationLevel) {
        switch (deprecationLevel) {
            case GOOD:
                return null;
            case OK:
                return Color.yellow;
            case BAD:
                return Color.red;
            default:
                return null;
        }
    }

    public static String prettyGson(final JsonObject newObj) {
        return gson.toJson(newObj);
    }

    public static String modifierToString(int mod) {
        StringJoiner sj = new StringJoiner(" ");

        if ((mod & ACC_BRIDGE) != 0) sj.add("[bridge]");
        if ((mod & ACC_SYNTHETIC) != 0) sj.add("[syntetic]");

        if ((mod & ACC_PUBLIC) != 0) sj.add("public");
        if ((mod & ACC_PROTECTED) != 0) sj.add("protected");
        if ((mod & ACC_PRIVATE) != 0) sj.add("private");

        /* Canonical order */
        if ((mod & ACC_ABSTRACT) != 0) sj.add("abstract");
        if ((mod & ACC_STATIC) != 0) sj.add("static");
        if ((mod & ACC_FINAL) != 0) sj.add("final");
        if ((mod & ACC_TRANSIENT) != 0) sj.add("transient");
        if ((mod & ACC_VOLATILE) != 0) sj.add("volatile");
        if ((mod & ACC_SYNCHRONIZED) != 0) sj.add("synchronized");
        if ((mod & ACC_NATIVE) != 0) sj.add("native");
        if ((mod & ACC_STRICT) != 0) sj.add("strictfp");
        if ((mod & ACC_INTERFACE) != 0) sj.add("interface");

        return sj.toString();
    }

    public static String toIl(int i) {
        return Integer.toBinaryString(i).replace('0', 'I').replace('1', 'l');
    }

    public static String replaceMainClass(String s, String main) {
        StringBuilder sb = new StringBuilder();

        for (String s1 : s.split("\n")) {
            if (s1.startsWith("Main-Class")) {
                sb.append("Main-Class: ").append(main);
            } else {
                sb.append(s1).append("\n");
            }
        }

        return sb.toString();
    }

    public static String getMainClass(String s) {
        String mainClass = null;

        for (String s1 : s.split("\n")) {
            if (s1.startsWith("Main-Class: ")) {
                mainClass = s1.substring("Main-Class: ".length()).trim().replace("\r", "");
            }
        }

        return mainClass;
    }


    public static String getInternalName(Type type) {
        switch (type.toString()) {
            case "V":
                return "void";
            case "Z":
                return "boolean";
            case "C":
                return "char";
            case "B":
                return "byte";
            case "S":
                return "short";
            case "I":
                return "int";
            case "F":
                return "float";
            case "J":
                return "long";
            case "D":
                return "double";
            default:
                throw new IllegalArgumentException("Type not known.");
        }
    }

    public static boolean checkZip(String file) {
        try {
            new ZipFile(file).entries();
        } catch (Throwable e) {
            return false;
        }
        return true;
    }
}
