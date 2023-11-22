package fileupload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Locale;


public class FileUtil {

    //파일 업로드
    public static String uploadFile(HttpServletRequest req, String sDirectory)
            throws ServletException, IOException {
        // 이 경로가 없으면 만들어줌. 상위 directory까지 전부...
        Path saveDirectoryPath = Paths.get(sDirectory);
        Files.createDirectories(saveDirectoryPath);

        Part part = req.getPart("ofile");

        Collection<Part> parts = req.getParts();

        String partHeader = part.getHeader("content-disposition");
        System.out.println("partHeader=" + partHeader);

        String[] phArr = partHeader.split("filename=");
        String originalFileName = phArr[1].trim().replace("\"", "");

        if (!originalFileName.isEmpty()) {
            part.write(sDirectory + File.separator + originalFileName);
        }
        return originalFileName;
    }

    //파일명 변경
    public static String renameFile(String sDirectory, String fileName) {
        String originalNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        String ext = fileName.substring(fileName.lastIndexOf("."));
        String now = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA).format(new Date());

        String newFileName = now + "__" + originalNameWithoutExtension + ext;

        //기존 파일명을 새로운 파일명으로 변경
        File oldFile = new File(sDirectory + File.separator + fileName);
        File newFile = new File(sDirectory + File.separator + newFileName);
        oldFile.renameTo(newFile);

        return newFileName;
    }

    //multiple 속성 추가로 2개 이상의 파일 업로드
    public static ArrayList<String> multipleFile(HttpServletRequest req, String sDirectory)
            throws ServletException, IOException {
        //파일명 저장을 위한 컬렉션 생성
        ArrayList<String> listFileName = new ArrayList<>();

        //Part 객체를 통해 서버로 전송된 파일명 읽어오기
        Collection<Part> parts = req.getParts();
        for(Part part : parts) {
            //파일이 아니라면 업로드의 대상이 아니므로 무시
            if(!part.getName().equals("ofile"))
                continue;

            //Part 객체의 헤더값 중 content-disposition 읽어오기
            String partHeader = part.getHeader("content-disposition");
            //출력결과 => form-data; name="attachedFile"; filename="파일명.jpg"
            System.out.println("partHeader="+ partHeader);

            //헤더값에서 파일명 잘라내기
            String[] phArr = partHeader.split("filename=");
            String originalFileName = phArr[1].trim().replace("\"", "");

            //전송된 파일이 있다면 디렉토리에 저장
            if (!originalFileName.isEmpty()) {
                part.write(sDirectory+ File.separator +originalFileName);
            }

            //컬렉션에 추가
            listFileName.add(originalFileName);
        }

        //원본 파일명 반환
        return listFileName;
    }

    //파일 다운로드
    public static void download(HttpServletRequest req, HttpServletResponse resp,
                                String directory, String sfileName, String ofileName) {
        String sDirectory = req.getServletContext().getRealPath(directory);
        try {
            // 파일을 찾아 입력 스트림 생성
            File file = new File(sDirectory, sfileName);
            InputStream iStream = new FileInputStream(file);

            // 한글 파일명 깨짐 방지
            String client = req.getHeader("User-Agent");
            if (client.indexOf("WOW64") == -1) {
                ofileName = new String(ofileName.getBytes("UTF-8"), "ISO-8859-1");
            }
            else {
                ofileName = new String(ofileName.getBytes("KSC5601"), "ISO-8859-1");
            }

            // 파일 다운로드용 응답 헤더 설정
            resp.reset();
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"" + ofileName + "\"");
            resp.setHeader("Content-Length", "" + file.length() );

            //out.clear();  // 출력 스트림 초기화

            // response 내장 객체로부터 새로운 출력 스트림 생성
            OutputStream oStream = resp.getOutputStream();

            // 출력 스트림에 파일 내용 출력
            byte b[] = new byte[(int)file.length()];
            int readBuffer = 0;
            while ( (readBuffer = iStream.read(b)) > 0 ) {
                oStream.write(b, 0, readBuffer);
            }

            // 입/출력 스트림 닫음
            iStream.close();
            oStream.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("파일을 찾을 수 없습니다.");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("예외가 발생하였습니다.");
            e.printStackTrace();
        }
    }

    //파일 삭제
    public static void deleteFile(HttpServletRequest req,
                                  String directory, String filename) {
        String sDirectory = req.getServletContext().getRealPath(directory);
        File file = new File(sDirectory + File.separator + filename);
        if (file.exists()) {
            file.delete();
        }
    }
}