/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8256811
 * @modules java.base/jdk.internal.org.objectweb.asm
 *          java.base/jdk.internal.misc
 * @library /test/lib
 * @run main/othervm/native TestClassUnloadEvents run
 */

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

import java.util.*;
import java.io.*;

public class TestClassUnloadEvents {
  static final String CLASS_NAME_PREFIX = "SampleClass__";
  static final int NUM_CLASSES = 10;

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
        runDebuggee();
    } else {
        runDebugger();
    }
  }

  private static class TestClassLoader extends ClassLoader implements Opcodes {
    private static byte[] generateSampleClass(String name) {
      ClassWriter cw = new ClassWriter(0);

      cw.visit(52, ACC_SUPER | ACC_PUBLIC, name, null, "java/lang/Object", null);
      MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "m", "()V", null, null);
      mv.visitCode();
      mv.visitInsn(RETURN);
      mv.visitMaxs(0, 0);
      mv.visitEnd();
      cw.visitEnd();
      return cw.toByteArray();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      if (name.startsWith(CLASS_NAME_PREFIX)) {
        byte[] bytecode = generateSampleClass(name);
        return defineClass(name, bytecode, 0, bytecode.length);
      } else {
        return super.findClass(name);
      }
    }
  }

  private static void runDebuggee() {
      System.out.println("Running debuggee");
      ClassLoader loader = new TestClassLoader();
      for (int index = 0; index < NUM_CLASSES; index++) {
          try {
            Class.forName(CLASS_NAME_PREFIX + index, true, loader);
          } catch (Exception e) {
            throw new RuntimeException("Failed to create Sample class");
          }
      }
      loader = null;
      System.gc();
  }

  private static void runDebugger() {
    System.out.println("Running debugger");
    HashSet<String> unloadedSampleClasses = new HashSet<>();
    VirtualMachine vm = null;
    try {
        vm = connectAndLaunchVM();
        ClassUnloadRequest classUnloadRequest = vm.eventRequestManager().createClassUnloadRequest();
        classUnloadRequest.addClassFilter(CLASS_NAME_PREFIX + "*");
        classUnloadRequest.enable();

        EventSet eventSet = null;
        boolean exited = false;
        while (!exited && (eventSet = vm.eventQueue().remove()) != null) {
            for (Event event : eventSet) {
            System.out.println("Event: " + event);
                if (event instanceof ClassUnloadEvent) {
                    String className = ((ClassUnloadEvent)event).className();
                    unloadedSampleClasses.add(className);
                }

                if (event instanceof VMDeathEvent) {
                    exited = true;
                    break;
                }
            }
            vm.resume();
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            char[] buf = new char[512];

            while (reader.read(buf) > 0) {
                writer.write(buf);
            }
            writer.flush();
        } catch (Exception e) {
        }
    }
    if (unloadedSampleClasses.size() != NUM_CLASSES) {
        throw new RuntimeException("Wrong number of class unload events: expected " + NUM_CLASSES + " got " + unloadedSampleClasses.size());
    }
  }

  private static VirtualMachine connectAndLaunchVM() throws Exception {
    LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
    Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
    arguments.get("main").setValue(TestClassUnloadEvents.class.getName());
    arguments.get("options").setValue("--add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED");
    return launchingConnector.launch(arguments);
  }
}
