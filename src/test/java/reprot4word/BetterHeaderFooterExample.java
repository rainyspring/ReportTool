package reprot4word;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class BetterHeaderFooterExample {

    public static void main(String[] args) throws IOException {
        XWPFDocument doc = new XWPFDocument();

        XWPFParagraph p = doc.createParagraph();

        XWPFRun r = p.createRun();
        r.setText("Some Text");
        r.setBold(true);
        r = p.createRun();
        r.setText("Goodbye");

        // create header/footer functions insert an empty paragraph
        XWPFHeader head = doc.createHeader(HeaderFooterType.DEFAULT);
        head.createParagraph().createRun().setText("header");
        
        XWPFFooter foot = doc.createFooter(HeaderFooterType.DEFAULT);
        foot.createParagraph().createRun().setText("footer");
        
        OutputStream os = new FileOutputStream(new File("header2.docx"));
        doc.write(os);
        os.close();
        doc.close();
    }
}
