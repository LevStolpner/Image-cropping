import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageCropper {

    public static byte[] cropImage(BufferedImage original, String format, int presetWidth, int presetHeight) throws Exception {
        BufferedImage croppedImage = cropProportionalImage(original, presetWidth, presetHeight);
        BufferedImage presetImage = croppedImage.getWidth() == presetWidth ? croppedImage : scaleImage(croppedImage, presetWidth, presetHeight);
        return writeImageToByteArray(format, presetImage);
    }

    private static BufferedImage cropProportionalImage(BufferedImage original, int presetWidth, int presetHeight) {
        int xOffset, yOffset, cropWidth, cropHeight;
        if (((double) original.getWidth()) / original.getHeight() > ((double) presetWidth) / presetHeight) {
            //if original image is "wider" than preset, then cropped image will take 100% of original image height
            cropWidth = (int) ((((double) presetWidth) / presetHeight) * original.getHeight());
            cropHeight = original.getHeight();
            xOffset = (original.getWidth() - cropWidth) / 2;
            yOffset = 0;
        } else {
            //if original image is "higher" than preset or have the same proportions, then cropped image will take 100% of original image width
            cropWidth = original.getWidth();
            cropHeight = (int) (((double) original.getWidth() * presetHeight) / presetWidth);
            xOffset = 0;
            yOffset = (original.getHeight() - cropHeight) / 2;
        }

        BufferedImage croppedImage = new BufferedImage(cropWidth, cropHeight, original.getType());
        Graphics2D g = croppedImage.createGraphics();
        g.drawImage(original, 0, 0, cropWidth, cropHeight, xOffset, yOffset, xOffset + cropWidth, yOffset + cropHeight, null);
        g.dispose();

        return croppedImage;
    }

    private static BufferedImage scaleImage(BufferedImage croppedImage, int presetWidth, int presetHeight) {
        BufferedImage presetImage = croppedImage;
        int tmpWidth = croppedImage.getWidth();
        int tmpHeight = croppedImage.getHeight();

        while (tmpWidth != presetWidth) {
            tmpWidth = tmpWidth / 2 < presetWidth ? presetWidth : tmpWidth / 2;
            tmpHeight = tmpHeight / 2 < presetHeight ? presetHeight : tmpHeight / 2;

            BufferedImage tmpImage = new BufferedImage(tmpWidth, tmpHeight, croppedImage.getType());
            Graphics2D g2 = tmpImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(presetImage, 0, 0, tmpWidth, tmpHeight, null);
            g2.dispose();

            presetImage = tmpImage;
        }

        return presetImage;
    }

    private static byte[] writeImageToByteArray(String format, BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output);
        ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
        ImageWriteParam writeParam = null;
        if (format.equals("jpg") || format.equals("jpeg")) {
            writeParam = writer.getDefaultWriteParam();
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(1.0f);
        }
        writer.setOutput(imageOutput);
        writer.write(null, new IIOImage(image, null, null), writeParam);
        writer.dispose();
        return output.toByteArray();
    }
}
