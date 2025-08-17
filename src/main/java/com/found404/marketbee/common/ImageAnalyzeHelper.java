package com.found404.marketbee.common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class ImageAnalyzeHelper {

    public static Result analyze(String filePath) throws IOException {
        BufferedImage img = ImageIO.read(new File(filePath));
        int w = img.getWidth(), h = img.getHeight();

        long sum=0, sumSq=0; int[] rgb = new int[w*h];
        img.getRGB(0,0,w,h,rgb,0,w);
        for (int p : rgb) {
            int r=(p>>16)&0xff, g=(p>>8)&0xff, b=p&0xff;
            int y=(int)Math.round(0.2126*r+0.7152*g+0.0722*b);
            sum += y; sumSq += (long) y*y;
        }
        double count=(double)w*h, mean=sum/count;
        double variance=(sumSq/count)-(mean*mean);
        double stddev=Math.sqrt(Math.max(variance,0));

        double topMean = regionMean(img, 0, 0, w, Math.max(1,(int)(h*0.2)));
        int cs = Math.max(0, h/2 - (int)(h*0.3)), ce = Math.min(h, h/2 + (int)(h*0.3));
        double center = regionMean(img, 0, cs, w, ce-cs);
        boolean backlightRisk = (topMean - center) > 25;

        return new Result(w, h, humanRatio(w,h), mean, stddev, backlightRisk);
    }

    private static double regionMean(BufferedImage img,int x,int y,int w,int h){
        long sum=0; int c=0;
        for(int j=y;j<y+h;j++) for(int i=x;i<x+w;i++){
            int p=img.getRGB(i,j);
            int r=(p>>16)&0xff, g=(p>>8)&0xff, b=p&0xff;
            int Y=(int)Math.round(0.2126*r+0.7152*g+0.0722*b);
            sum+=Y; c++;
        }
        return sum/(double)c;
    }

    private static String humanRatio(int w,int h){
        double r=(double)w/h, best=Double.MAX_VALUE; String ans=String.format(Locale.ROOT,"%.3f",r);
        double[][] cand={{1,1},{4,3},{3,2},{16,9},{5,4},{4,5}};
        for(double[] c:cand){ double rr=c[0]/c[1], d=Math.abs(rr - r);
            if(d<best){ best=d; ans=((int)c[0])+":"+((int)c[1]);}}
        return ans;
    }

    // ✅ 통계 전용 결과(문장 없음)
    public record Result(int width,int height,String aspectRatio,
                         double meanBrightness,double contrastStd,boolean backlightRisk) {}
}
