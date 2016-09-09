package cn.dreampie.common.util.scan;

import cn.dreampie.common.http.Encoding;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-19.
 */
public abstract class Scaner<S> {

  protected ClassLoader classLoader = Scaner.class.getClassLoader();
  protected boolean scanInJar;
  protected String targetPattern;
  protected String targetSuffix;
  protected Set<String> includePathOrPackages = new HashSet<String>();

  protected boolean checkTarget(Object target) {
    return true;
  }

  protected String packageFilePathSolve(String filePath) {
    return filePath;
  }

  protected String jarFilePathSolve(String filePath) {
    return filePath;
  }

  protected Enumeration<URL> urlSolve(String baseDir) {
    try {
      if (!baseDir.contains("/") && baseDir.contains(".")) {
        baseDir = baseDir.replaceAll("\\.", "/");
      }
      return classLoader.getResources(baseDir);
    } catch (IOException e) {
      throw new ScanException(e.getMessage(), e);
    }
  }

  protected S scanInJar(boolean scanInJar) {
    this.scanInJar = scanInJar;
    return (S) this;
  }

  protected S targetPattern(String targetPattern) {
    this.targetPattern = targetPattern;
    if (!targetPattern.contains(".")) {
      throw new ScanException("TargetPattern must contains '.'");
    }

    targetSuffix = targetPattern.substring(targetPattern.indexOf("."));
    return (S) this;
  }


  /**
   * scan to Class
   *
   * @param <T> 返回的lcass类型
   * @return 搜索到的class
   */
  public <T> Set<Class<? extends T>> scanToClass() {
    Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();
    Set<String> fileSet = scan();
    if (fileSet.size() > 0) {
      for (String classFile : fileSet) {
        Class<?> classInFile;
        try {
          classInFile = classLoader.loadClass(classFile);
        } catch (ClassNotFoundException e) {
          throw new ScanException(e.getMessage(), e);
        }
        if (checkTarget(classInFile)) {
          result.add((Class<? extends T>) classInFile);
        }
      }

    }
    return result;
  }

  /**
   * scan to File
   *
   * @return
   */
  public Set<File> scanToFile() {
    Set<File> result = new HashSet<File>();
    Set<String> fileSet = scan();
    if (fileSet.size() > 0) {
      File file;
      for (String path : fileSet) {
        file = new File(path);
        if (file.exists()) {
          result.add(file);
        }
      }
    }
    return result;
  }


  public Set<String> scan() {
    Set<String> fileSet = new HashSet<String>();
    if (includePathOrPackages.size() > 0) {
      for (String pkg : includePathOrPackages) {
        fileSet.addAll(findFiles(pkg, targetPattern));
      }
    }
    return fileSet;
  }

  /**
   * 递归查找文件
   *
   * @param baseDirName    查找的文件夹路径
   * @param targetFileName 需要查找的文件名
   */
  protected Set<String> findFiles(String baseDirName, String targetFileName) {
    /**
     * 算法简述： 从某个给定的需查找的文件夹出发，搜索该文件夹的所有子文件夹及文件， 若为文件，则进行匹配，匹配成功则加入结果集，若为子文件夹，则进队列。 队列不空，重复上述操作，队列为空，程序结束，返回结果。
     */
    Set<String> allFiles = new HashSet<String>();
    //判断文件路径
    Enumeration<URL> baseURLs = urlSolve(baseDirName);
    URL baseURL = null;
    if (baseURLs != null) {
      while (baseURLs.hasMoreElements()) {
        baseURL = baseURLs.nextElement();
        if (baseURL != null) {
          // 得到协议的名称
          String protocol = baseURL.getProtocol();
          String basePath = baseURL.getFile();

          // 如果是以文件的形式保存在服务器上
          if (scanInJar && "jar".equals(protocol)) {
            String[] paths = basePath.split("!/");
            // 获取jar
            try {
              allFiles.addAll(findJarFiles(URLDecoder.decode(paths[0].replace("file:", ""), Encoding.UTF_8.name()), paths[1]));
            } catch (IOException e) {
              throw new ScanException(e.getMessage(), e);
            }
          } else {
            allFiles.addAll(findPackageFiles(basePath, targetFileName));
          }
        }
      }
    }
    return allFiles;
  }

  /**
   * 查找根目录下的文件
   *
   * @param basePathName   路径
   * @param targetFileName 文件匹配
   * @return Set
   */
  private Set<String> findPackageFiles(String basePathName, String targetFileName) {
    Set<String> packageFiles = new HashSet<String>();
    String tempName = null;
    // 判断目录是否存在
    File baseFile = null;
    try {
      baseFile = new File(URLDecoder.decode(basePathName, Encoding.UTF_8.name()));
    } catch (UnsupportedEncodingException e) {
      throw new ScanException(e.getMessage(), e);
    }

    String filePath = null;

    if (!baseFile.exists()) {
      throw new ScanException("Search error : " + basePathName + " not found.");
    } else {
      if (baseFile.isDirectory()) {
        String[] fileList = baseFile.list();
        if (fileList != null && fileList.length > 0) {
          for (String file : fileList) {
            File readfile = null;
            try {
              readfile = new File(URLDecoder.decode(basePathName + File.separator + file, Encoding.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
              throw new ScanException(e.getMessage(), e);
            }
            //文件目录
            if (readfile.isDirectory()) {
              packageFiles.addAll(findPackageFiles(basePathName + File.separator + file, targetFileName));
            } else {
              tempName = readfile.getName();
              //判断是否是目标文件
              if (matchTargetPattern(targetFileName, tempName)) {
                filePath = readfile.getAbsoluteFile().toString().replaceAll("\\\\", "/");
                packageFiles.add(packageFilePathSolve(filePath));
              }
            }
          }
        }
      } else {
        tempName = baseFile.getName();
        //判断是否是目标文件
        if (matchTargetPattern(targetFileName, tempName)) {
          filePath = baseFile.getAbsoluteFile().toString().replaceAll("\\\\", "/");
          packageFiles.add(packageFilePathSolve(filePath));
        }
      }

    }
    return packageFiles;
  }

  /**
   * find jar file
   *
   * @param filePath    文件路径
   * @param packageName 包名
   * @return list
   * @throws IOException 文件读取异常
   */
  private Set<String> findJarFiles(String filePath, String packageName) throws IOException {
    JarFile localJarFile = new JarFile(new File(filePath));
    Set<String> inJarFiles = findInJarFiles(localJarFile, packageName);
    localJarFile.close();
    return inJarFiles;
  }

  /**
   * fin in jar files
   *
   * @param localJarFile
   * @param packageName
   * @return
   */
  private Set<String> findInJarFiles(JarFile localJarFile, String packageName) {
    Set<String> inJarFiles = new HashSet<String>();
    Enumeration<JarEntry> entries = localJarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry jarEntry = entries.nextElement();
      String entryName = jarEntry.getName();
      if (!jarEntry.isDirectory() && (packageName == null || entryName.startsWith(packageName)) && entryName.endsWith(targetSuffix)) {
        inJarFiles.add(jarFilePathSolve(entryName));
      }
    }
    return inJarFiles;
  }

  public Scaner include(String... pathOrPackages) {
    checkNotNull(pathOrPackages, "File path or packages could not be null.");
    Collections.addAll(includePathOrPackages, pathOrPackages);
    return this;
  }

  public Scaner include(Set<String> pathOrPackages) {
    checkNotNull(pathOrPackages, "File path or packages could not be null.");
    for (String pkg : pathOrPackages) {
      this.includePathOrPackages.add(pkg);
    }
    return this;
  }


  /**
   * 通配符匹配
   *
   * @param pattern 通配符模式
   * @param str     待匹配的字符串
   *                匹配成功则返回true，否则返回false
   */
  private boolean matchTargetPattern(String pattern, String str) {
    int patternLength = pattern.length();
    int strLength = str.length();
    int strIndex = 0;
    char ch;
    for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
      ch = pattern.charAt(patternIndex);
      if (ch == '*') {
        // 通配符星号*表示可以匹配任意多个字符
        while (strIndex < strLength) {
          if (matchTargetPattern(pattern.substring(patternIndex + 1), str.substring(strIndex))) {
            return true;
          }
          strIndex++;
        }
      } else if (ch == '?') {
        // 通配符问号?表示匹配任意一个字符
        strIndex++;
        if (strIndex > strLength) {
          // 表示str中已经没有字符匹配?了。
          return false;
        }
      } else {
        if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
          return false;
        }
        strIndex++;
      }
    }
    return strIndex == strLength;
  }

}