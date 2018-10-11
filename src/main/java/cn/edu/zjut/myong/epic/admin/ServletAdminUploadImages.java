package cn.edu.zjut.myong.epic.admin;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 启动一个agent的servlet。和其他servlet不同，因为需要传递文件，所以使用POST而不是GET。
 * @version 0.0.1
 * @author Yong Min (minyung@yahoo.com)
 */
public class ServletAdminUploadImages extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        try {
            // Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp coordinate is used)
            ServletContext servletContext = this.getServletConfig().getServletContext();
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            // 清理文件
            FileCleaningTracker cleaner = FileCleanerCleanup.getFileCleaningTracker(servletContext);
            factory.setFileCleaningTracker(cleaner);

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // 准备写入临时文件
            for (int i = 0; i < items.size(); i++) {
                File f = new File("./web/images/uploading/" + items.get(i).getName().replace(' ', '_'));
                if (f.exists() && !f.isDirectory()) {
                    // i--;
                    continue;
                }
                f.createNewFile();
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(items.get(i).get());
                fos.close();
                AdminStation.uploadImage(items.get(i).getName(), "/images/uploading/" + items.get(i).getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("UPLOAD FILE ERROR");
            return;
        }

        response.getWriter().print("OK");
    }
}
