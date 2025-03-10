package main.java.com.company;

import static org.opencv.highgui.Highgui.imread;
import static org.opencv.highgui.Highgui.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mysql.jdbc.StringUtils;
import net.sourceforge.tess4j.ITesseract;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;


import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.imgproc.Imgproc;
import org.w3c.dom.ls.LSOutput;

import javax.imageio.ImageIO;

public class Main {

    private static final int CV_ADAPTIVE_THRESH_MEAN_C = 1;
    private static final int CV_THRESH_BINARY = 0;
    private static final int CV_THRESH_OTSU = 8;
    private static final int CV_ADAPTIVE_THRESH_GAUSSIAN_C = 1;
    private static final int CV_THRESH_BINARY_INV = 1;
    public static GuiClass g;

    public static String texta;
    // Source path content images
    static String SRC_PATH = "C:\\Recognize\\java_text";
    static String TESS_DATA = "C:\\Program Files\\Tesseract-OCR\\tessdata";

    // Create tess obj
    static Tesseract tesseract = new Tesseract();

    //hold the resultString
    public static String rString;
    public static List<String> rtun = new ArrayList<>(10);

    // Load OPENCV
    static {
        String libPath = System.getProperty("C:\\opencv\\build\\java\\x64");

        String libraryName = "opencv_java411";
        //System.out.println("Trying to load '" + libraryName + "'");
        System.loadLibrary(libraryName);
        tesseract.setDatapath(TESS_DATA);
        tesseract.setLanguage("tur");
    }

    String extractTextFromImage(Mat inputMat) {
        String result = "";
        Mat gray = new Mat();

        // Convert to gray scale
        cvtColor(inputMat, gray, COLOR_BGR2GRAY);
        imwrite(SRC_PATH + "gray.png", gray);

        //  Apply closing, opening
        //Mat element = getStructuringElement(MORPH_RECT, new Size(2, 2), new Point(1, 1));
        //dilate(gray, gray, element);
        //erode(gray, gray, element);

        //imwrite(SRC_PATH + "closeopen.png", gray);

        try {
            // Recognize text with OCR
            result = tesseract.doOCR(new File(SRC_PATH + "gray.png"));
        } catch (TesseractException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Main(){

    }


    public void baslat() {

        g = new GuiClass();
        g.anafonk();


    }


    String sTarih;
    String sFisNo;
    ArrayList<String> urun = null;

    String as;

    String sirket;

    //[a-zA-Z]+[0-9]{2}



    public void tesseract(String path) {


        System.out.println("Start recognize text from image");
        long start = System.currentTimeMillis();

        System.out.println(path);
        // Read image
        //Mat origin = imread(path);

        //Mahmut tries something and it doesn't work
        Mat img = Imgcodecs.imread(path);
        Imgcodecs.imwrite("preprocess/True_Image.png", img);
        //grayScale
        Mat imgGray = new Mat();
        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("preprocess/Gray.png", imgGray);
        //Gaussian Blur
        Mat imgGaussianBlur = new Mat();
        Imgproc.GaussianBlur(imgGray,imgGaussianBlur,new Size(3, 3),0);
        Imgcodecs.imwrite("preprocess/gaussian_blur.png", imgGaussianBlur);
/*      //Sobel
        Mat imgSobel = new Mat();
        Imgproc.Sobel(imgGaussianBlur, imgSobel, -1, 1, 0);
        Imgcodecs.imwrite("preprocess/4_imgSobel.png", imgSobel);


    Sobel bak moruk
*/
        //Step5
        Mat imgThreshold = new Mat();
        Imgproc.threshold(imgGaussianBlur, imgThreshold, 0, 255,  CV_THRESH_OTSU + CV_THRESH_BINARY);
        Imgcodecs.imwrite("preprocess/5_imgThreshold.png", imgThreshold);

        //Adaptive Threshold
        Mat imgAdaptiveThreshold = new Mat();
        Imgproc.adaptiveThreshold(imgThreshold, imgAdaptiveThreshold, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY_INV, 99, 50);
        Imgcodecs.imwrite("preprocess/5_imgAdaptiveThreshold.png", imgAdaptiveThreshold);


//bu bizim eskisi
        Imgproc.adaptiveThreshold(imgGaussianBlur, imgAdaptiveThreshold, 255, CV_ADAPTIVE_THRESH_MEAN_C ,CV_THRESH_BINARY, 79, 27);
        Imgcodecs.imwrite("preprocess/adaptive_threshold2.png", imgAdaptiveThreshold);


        grayscale(path);


        File imageFile = new File("preprocess/grayscale.tiff");
        ITesseract instance = new Tesseract();
        instance.setDatapath(TESS_DATA);
        instance.setLanguage("tur");

        String result = null;
        try {
            result = instance.doOCR(imageFile);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        rString = result;
        as = result;
        System.out.println(result);

//        String res = new Main().extractTextFromImage(imgAdaptiveThreshold);
//        System.out.println(res);
        //String result = new Main().extractTextFromImage(origin);
        //rString = result;
        //System.out.println(result);

        System.out.println("Time");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("Done");

        try {
            splitter();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Connect connect = new Connect();
        connect.connectDatabase();

    }


    //grayscale
    public void grayscale(String path){
        BufferedImage img = null;
        File f = null;

        //read image
        try{
            f = new File(path);
            img = ImageIO.read(f);
        }catch(IOException e){
            System.out.println(e);
        }

        //get image width and height
        int width = img.getWidth();
        int height = img.getHeight();

        //convert to grayscale
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int p = img.getRGB(x,y);

                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                //calculate average
                int avg = (r+g+b)/3;

                //replace RGB value with avg
                p = (a<<24) | (avg<<16) | (avg<<8) | avg;

                img.setRGB(x, y, p);
            }
        }


        //write image


        try{
            f = new File("preprocess/grayscale.tiff");
            //f = new File("preprocess/adaptive_threshold2.png");
            //f = new File("preprocess/Gray.png");
            //f = new File("preprocess/gaussian_blur.png");

            ImageIO.write(img, "tiff", f);

        }catch(IOException e){
            System.out.println(e);
        }
    }


    //String paraçalama işi burada dönüyor
    public void splitter() throws SQLException {

        //tarih
        sTarih = regexChecker("\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\s|([0-2][0-9]|" +
                "(3)[0-1])(\\/)(((0)[0-9])|((1)[0-2]))(\\/)\\d{4}|\\s*(3[01]|[12][0-9]|0?[1-9])\\. (1[012]|0?[1-9]) \\. ((?:19|20)\\d{2})\\s|([0-2][0-9]|(3)[0-1])(\\/)(((0)[0-9])|((1)[0-2]))(\\/)\\d{4}|([0-2][0-9]|(3)[0-1])(\\,)(((0)[0-9])|((1)[0-2]))(\\.)\\d{4}", rString);
        System.out.println("Tarih: "+sTarih);
        if (sTarih != null){
            if (sTarih.contains(",")) {
                sTarih = sTarih.replace(",", "/");
            }
            if (sTarih.contains(".")) {
                sTarih = sTarih.replace(".", "/");
            }
            if (sTarih.contains("-")) {
                sTarih = sTarih.replace("-", "/");
            }
        }else {
            sTarih = "18/10/2019";
        }



        //Fiş no splitter
        as = clearTurkishChars(as);
        int fis = 0;
        sFisNo = regexChecker("(.*?.*:?fıs no.*)|(.*?.*:?fis no.*)|(.*?.*:?fıs n.*)|(.*?.*:?fiis n.*)|(.*?.*:?fııs n.*)|(.*?.*:?FiİŞ n.*)|(.*?.*:?fis n.*)| (.*?.*:?fış n.*) | (.*?.*:?FIŞ NG.*)| (.*?.*:?fis n.*)|(.*?.*:?fis n.*)|(.*?.*:?fi\\$ n.*)|(.*?.*:?fisno.*)|(.*?.*:?fis- n.*)", as);
        System.out.println("fiş no :" + sFisNo);
        if (sFisNo != null){
            sFisNo = regexChecker("[0-9]{2,4}", sFisNo);
            fis = Integer.parseInt(sFisNo);
            System.out.println("fiş no :" + sFisNo);

        }


        //Şirket adını Stringten çektiğimiz kısım

        sirket = regexChecker("^.*\\r?\\n(.*)", as);
        String lines[] = sirket.split("\\r?\\n");

        if (sirket.contains("tesekk")){
            sirket = lines[1];
        } else {
            if (lines[0].contains("a.s") || lines[0].contains("market") || lines[0].contains("tic") || lines[0].contains("gıda")||
                    lines[0].contains("eczane")|| lines[0].contains("paz")|| lines[0].contains("san")) {
                sirket = lines[0];
            }else {
                sirket = sirket;
            }
        }

        sirket = sirket.replace("\n", " ").replace("\r","").replace("î", "i");
        sirket = sirket.replace("?", "i").replace("ı", "i").replace("'", "").replace("â","a");
        System.out.println(sirket);


        //Urun
        regexer("(.*?.*:?(...)x08.*)|(.*?.*:?(...)408.*)|(.*?.*:?(...)%08.*)|(.*?.*:?(...)X08.*)|(.*?.*:?(...)\\s\\*08.*)|" +
                "(.*?.*:?(...)x18.*)|(.*?.*:?(...)\\s418\\s.*)|(.*?.*:?(...)\\s38.*)|(.*?.*:?(...)%18.*)|(.*?.*:?(...)X18.*)|" +
                "(.*?.*:?(...)\\*18.*)|(.*?.*:?(...)x01.*)|(.*?.*:?(...)\\s401.*)|(.*?.*:?(...)%01.*)|(.*?.*:?(...)X01.*)|(.*?.*:?(...)\\*01\\s.*)|" +
                "(.*?.*:?(...)x8.*)|(.*?.*:?(...)\\s48\\s.*)|(.*?.*:?(...)%8.*)|(.*?.*:?(...)X8.*)|(.*?.*:?(...)\\*8\\s.*)|(.*?.*:?(...)\\s08\\s.*)|(.*?.*:?(...)\\s78\\s.*)", as );
        rtun.forEach((n) -> System.out.println(n));

        //KDV
//        sKDV = regexChecker("X+[0-9]{2}|x+[0-9]{2}", rString);
//        sKDV = regexChecker("[0-9]{2}", sKDV);
//        System.out.println(sKDV);

        String sToplam1;
        String sToplam;
        //Toplam
        sToplam1 = regexChecker("TOPLAM+ \\*[0-9]{1,4}, [0-9]{2}|TOPLAM+ \\*[0-9]{1,4},[0-9]{2}|TOPLAM+ \\*[0-9]{1,4} , [0-9]{2}|TOP\\s \\*[0-9]{1,4},[0-9]{2}|TOP \\*[0-9]{1,4},[0-9]{2}|TOPLAM x[0-9]{1,4},[0-9]{2}|TOPLAM+ \\*[0-9]{1,4} ,[0-9]{2}",as);
        if (sToplam1 != null){
            sToplam = regexChecker("[0-9]{1,4}, [0-9]{2}|[0-9]{1,4},[0-9]{2}|[0-9]{1,4} , [0-9]{2}|[0-9]{1,4} ,[0-9]{2}" ,sToplam1);
        }else {
            sToplam = "30,95";
        }


        System.out.println("Toplam : " + sToplam1);


        //DATE  replace / with -
        Connect connect = new Connect();
        String sDate1=sTarih;
        java.sql.Date sqlDate = null;
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
            Date date = sdf1.parse(sDate1);
            sdf1.applyPattern("yyyy-MM-dd");
            sDate1 = sdf1.format(date);
            sqlDate = new java.sql.Date(date.getTime());



        } catch (ParseException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("------*-------*-----------*---------");
        Object[] array = rtun.toArray();

        for (int i = 0; i < rtun.size(); i++) {
            array[i] = rtun.get(i).replace("x", "%");
        }

        for (int i = 0; i < rtun.size(); i++) {
            array[i] = (String)array[i].toString().replace("?", "i").replace("ı", "i").replace("4", "%");
        }
        for (int i = 0; i < array.length; i++) {

            connect.checkDB(sirket, sqlDate, fis, (String) array[i], sToplam);

            System.out.println("ürün = "+rtun.get(i));
        }

        StringBuffer sb = new StringBuffer();

        for (String s : rtun) {
            sb.append(s);
            sb.append("\n");
        }
        String str = sb.toString();
        str = str.replace("4", "%").replace("x", "%").replace("?", "i").replace("ı", "i");
        String f = "" + fis;

        texta ="-*- Okunan Fiş -*-\n\n\n"
                +"Şirket Adı: "+ sirket
                +"\n\nFiş No: "+ f
                +"\n\nTarih: " + sDate1
                +"\n\nÜrünler; \n" + str
                +"\n\nToplam: " + sToplam;

        g.showOnTable(1);
        //send to db



    }

    public static String clearTurkishChars(String str) {
        String ret = str;
        char[] turkishChars = new char[] {0x131, 0x130, 0xFC, 0xDC, 0xF6, 0xD6, 0x15F, 0x15E, 0xE7, 0xC7, 0x11F, 0x11E};
        char[] englishChars = new char[] {'i', 'I', 'u', 'U', 'o', 'O', 's', 'S', 'c', 'C', 'g', 'G'};
        for (int i = 0; i < turkishChars.length; i++) {
            ret = ret.replaceAll(new String(new char[]{turkishChars[i]}), new String(new char[]{englishChars[i]}));
        }
        return ret;
    }





    //Regex Checker Function
    public static String regexChecker(String theRegex, String str2Check){
        String returner = null;
        // You define your regular expression (REGEX) using Pattern

       // Pattern checkRegex = Pattern.compile(theRegex);

        str2Check = str2Check.toLowerCase();

        Pattern checkRegex = Pattern.compile(theRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        // Creates a Matcher object that searches the String for
        // anything that matches the REGEX

        Matcher regexMatcher = checkRegex.matcher( str2Check );

        // Cycle through the positive matches and print them to screen
        // Make sure string isn't empty and trim off any whitespace

        while ( regexMatcher.find() ){
            if (regexMatcher.group().length() != 0){
                //System.out.println( regexMatcher.group().trim() );
                returner = regexMatcher.group().trim();
                // You can get the starting and ending indexs
            }
        }

        System.out.println();
        return returner;
    }


    //Regex Checker Function
    public static void  regexer(String theRegex, String str2Check){

        // You define your regular expression (REGEX) using Pattern

        // Pattern checkRegex = Pattern.compile(theRegex);

        str2Check = str2Check.toLowerCase();

        Pattern checkRegex = Pattern.compile(theRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        // Creates a Matcher object that searches the String for
        // anything that matches the REGEX

        Matcher regexMatcher = checkRegex.matcher( str2Check );


        // Cycle through the positive matches and print them to screen
        rtun.clear();
        // Make sure string isn't empty and trim off any whitespace
        String line;
        while ( regexMatcher.find() ){
            if (regexMatcher.group().length() != 0){
                line = regexMatcher.group().trim();
                rtun.add(line);
                // You can get the starting and ending indexs
            }
        }

        System.out.println();
    }


}
