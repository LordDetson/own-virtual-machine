package by.babanin.vm;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import by.babanin.vm.factory.VirtualMachineFactory;

public class Launcher {

    public static void main(String[] args) {
        Path path = Paths.get(args[0]);
        byte[] programBytes = readFile(path);
        short origin = getOrigin(programBytes);
        String program = getProgram(programBytes);

        VirtualMachineFactory virtualMachineFactory = new VirtualMachineFactory();
        VirtualMachine virtualMachine = virtualMachineFactory.lc3VirtualMachine();
        virtualMachine.writeProgram(origin, program);
        virtualMachine.run();
    }

    private static byte[] readFile(Path path) {
        try (FileInputStream stream = new FileInputStream(path.toFile())){
            return stream.readAllBytes();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static short getOrigin(byte[] programBytes) {
        short origin = (short) (programBytes[0] << 8);
        origin += programBytes[1];
        return origin;
    }

    private static String getProgram(byte[] programBytes) {
        StringBuilder builder = new StringBuilder();
        for(int i = 2; i < programBytes.length; i += 2) {
            byte secondByte = programBytes[i];
            byte firstByte = programBytes[i + 1];
            builder.append(expand(secondByte));
            builder.append(expand(firstByte));
        }
        return builder.toString();
    }

    private static String expand(byte value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }
}
