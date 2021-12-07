import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

/**
 * 	对节点进行操作时，一定要先选中节点
 * @author 陆于洋
 */
public class ResManager extends JFrame
				implements TreeSelectionListener, MouseListener, ActionListener {
	//设置树结构
	private JTree tree = new JTree();
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode top;
	//设置右击菜单项
	private JPopupMenu jp = new JPopupMenu();
	private JMenuItem copy = new JMenuItem("复制");
	private JMenuItem paste = new JMenuItem("粘贴");
	private JMenuItem delete = new JMenuItem("删除");
	//设置复制黏贴临时存储
	private File copy_path = null;
	private File now_path = null;
	private DefaultMutableTreeNode now_node = null;
	//设置中间面板
	JPanel jpl = new JPanel();
	
	/**
	 * 	构造函数
	 */
	public ResManager() {
		//设置布局
		jpl.setLayout(new FlowLayout());
		this.setLayout(new BorderLayout());
		this.add(jpl, BorderLayout.CENTER);
		this.add(createPW(), BorderLayout.WEST);
		//右键的事件监听
		ini_right_click();
		//设置窗体
		this.setSize(550, 450);					//设置大小
		this.setVisible(true);					//设置窗体可见
		this.setTitle("资源管理器");				//设置标题
		this.setResizable(false);				//设置不允许缩放
		setLocationRelativeTo(null);			//窗口居中
        setDefaultCloseOperation(EXIT_ON_CLOSE);//窗口点击关闭时,退出程序
	}
	/**
	 * 	设置西边面板
	 * @return
	 */
	public JPanel createPW() {
		JPanel pw = new JPanel();
		//设置树的模式,也可以不设置
		top = new DefaultMutableTreeNode("我的电脑");
		treeModel = new DefaultTreeModel(top);
		//设置树
		tree = new JTree(treeModel);		//可以直接用节点初始化
		//tree.setEditable(true);				//设置可以编辑节点名
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);//设置仅单选
		tree.setShowsRootHandles(true);
		//设置监听
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(this);
		//设置磁盘节点,加在top上	
		File roots[] = File.listRoots();
		for(int i=0; i<roots.length; i++) {
			DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(roots[i]);
			top.add(tmp);
			createNode(roots[i], tmp);
		}
		//添加至面板
        JScrollPane jsp = new JScrollPane(tree);
        jsp.setPreferredSize(new Dimension(200, 405));
		pw.add(jsp);
		//返回
		return pw;
	}
	/**
	 * 	创建节点
	 * @param path	文件路径
	 * @param node	默认节点
	 */
	public void createNode(File path, DefaultMutableTreeNode node) {
		//如果是文件,则跳出
		if(path.isFile())
			return;
		//否则,获取子文件,创建子节点
		File childs[] = path.listFiles();
		for(int i=0; i<childs.length; i++) {
			//如果是隐藏文件，则跳过
			if(childs[i].isHidden())
				continue;
			//根据文件名,创建节点
			DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(childs[i].getName());
			//加入树中
			treeModel.insertNodeInto(tmp, node, node.getChildCount());
			//如果是文件夹,则创建空子节点(在后面会删掉)
			if(childs[i].isDirectory())
				tmp.add(new DefaultMutableTreeNode());

		}
	}
	/**
	 * 	初始化右键菜单
	 */
	public void ini_right_click() {
		jp.add(copy);
        jp.add(paste);
        jp.add(delete);
        copy.addActionListener(this);
        paste.addActionListener(this);
        delete.addActionListener(this);
	}
	/**
	 * 	主函数
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自动生成的方法存根
		new ResManager();
	}
	
	//选择节点时，往下更新子节点
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// TODO 自动生成的方法存根
		//获取选择的节点
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) (e.getPath().getLastPathComponent());
		now_node = node;
		//若为根节点，跳出
		if(node.isRoot())
			return;
		//根据获取到的树中路径,创建磁盘绝对路径
		TreeNode[] tp = node.getPath();
		String path = tp[1].toString();
		for(int i=2; i<tp.length; i++) {
			path += tp[i] + "\\";
		}
		File node_file = new File(path);
		//删除子文件
		node.removeAllChildren();
		SwingUtilities.invokeLater(new Runnable(){ 
			public void run(){
				//右侧文件显示
				if(!node_file.isFile()) {
					jpl.removeAll();
					File roots[] = node_file.listFiles();
					for(int i=0; i<roots.length; i++) {
						if(!roots[i].isHidden())
							jpl.add(new JLabel(roots[i].getName()));
					}
					jpl.updateUI();
				}
				//树结构
				tree.updateUI();
		}});//处理updateUI()抛空指针的问题
		//增加节点函数
		if(!node_file.isFile())
			createNode( node_file, node);
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO 自动生成的方法存根
		//获取右击点的节点路径
		TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		//右击事件
		if (e.getButton() == MouseEvent.BUTTON3 && path != null) {
			// 确定当前节点
			Object tmp[] = path.getPath();
			if(tmp.length == 1)	//如果获取的字符串长度为1,即是最上一层,则跳出
				return;
			String tmp_path = tmp[1].toString();
			for(int i=2; i<tmp.length; i++) {
				tmp_path += tmp[i] + "\\";
			}
			now_path = new File(tmp_path);
            // 弹出菜单
            jp.show(e.getComponent(), e.getX(), e.getY());
        }
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO 自动生成的方法存根
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO 自动生成的方法存根
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO 自动生成的方法存根
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO 自动生成的方法存根
		
	}
	/**
	 * 	实现右键功能
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO 自动生成的方法存根
		if(e.getSource() == copy) {	//复制按钮
			copy_path = now_path;
		}else if (e.getSource() == paste) {	//粘贴按钮
			now_node.add(new DefaultMutableTreeNode(copy_path.getName()));
			//如果复制的节点下面还有子节点,也一起复制
			if(copy_path.isDirectory())
				createNode(copy_path, now_node);
		}else if(e.getSource() == delete) {	//删除按钮
			treeModel.removeNodeFromParent(now_node);
		}
	}
}