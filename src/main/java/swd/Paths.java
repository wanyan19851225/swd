package swd;

public interface Paths {
	
	javax.swing.filechooser.FileSystemView fsv = javax.swing.filechooser.FileSystemView.getFileSystemView();
	String usrdocment=fsv.getDefaultDirectory().toString();	//获取"我的文档"路径，默认将检索文件存放在"我的文档"目录下
	
	public static final String repositorypath  = usrdocment+"\\swd\\repository\\index";
	public static final String itempath = usrdocment+"\\swd\\items\\index";
	public static final String filepath = usrdocment+"\\swd\\files\\index";

}
