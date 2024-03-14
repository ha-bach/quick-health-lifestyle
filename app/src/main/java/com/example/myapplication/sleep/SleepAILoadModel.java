package com.example.myapplication.sleep;

import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class SleepAILoadModel {
    private Interpreter interpreter;

    public SleepAILoadModel(String modelPath) {
        try {
            interpreter = new Interpreter(loadModelFile(modelPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(modelPath);
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = fileChannel.position();
        long declaredLength = fileChannel.size();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[] predict(float[] input) {
        float[][] output = new float[1][1];
        interpreter.run(input, output);
        return output[0];
    }
}
