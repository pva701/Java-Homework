import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by pva701 on 5/19/15.
 */
public class JExplorerForm extends JFrame {
    private JPanel rootPanel;
    private JTable table1;
    private JTree tree;

    private DefaultTreeModel treeModel;

    private String toStringTreePath(TreePath path) {
        Object[] pathArr = path.getPath();
        String strPath = "/";
        for (int i = 1; i < pathArr.length; ++i) {
            strPath += pathArr[i];
            if (i + 1 != pathArr.length) strPath += "/";
        }
        return strPath;
    }

    public JExplorerForm() {
        super("JExplorer");
        //tree.setLargeModel();
        setContentPane(rootPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        /*tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                System.out.println("clicked");
            }


        });*/

        /*tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                System.out.println("sel row = " + selRow);
                TreePath selPath = tree.getPathForRow(selRow);
                System.out.println("sel path " + selPath);
                if (selRow != -1 && e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node == null)
                        return;
                    String strPath = toStringTreePath(selPath);
                    System.out.println("strPath = " + strPath);
                    node.removeAllChildren();
                    addChildFiles(new File(strPath), node);
                    treeModel.reload(node);
                }
            }
        });*/

        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                System.out.println("here");
                TreePath selPath = event.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();
                System.out.println("node = " + node);
                if (node == null)
                    return;
                String strPath = toStringTreePath(selPath);
                System.out.println("strPath = " + strPath);
                node.removeAllChildren();
                addChildFiles(new File(strPath), node);
                treeModel.reload(node);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

            }
        });

        setVisible(true);
        pack();
    }

    private boolean isEmptyDirectory(File directory) {
        String[] names = directory.list();
        return names != null && names.length == 0;
    }

    private void addChildFiles(File directory, DefaultMutableTreeNode node) {
        String str = "/";
        TreeNode[] nodes = node.getPath();
        for (int i = 1; i < nodes.length; ++i) {
            str += nodes[i];
            if (i + 1 != nodes.length) str += "/";
        }
        String[] names = directory.list();
        if (names == null)
            return;
        Arrays.sort(names);
        for (String name : names) {
            if (name.charAt(0) == '.') continue;
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(name);
            if (Files.isDirectory(Paths.get(str + "/" + name))) child.add(new DefaultMutableTreeNode("."));
            node.add(child);
        }
    }

    private void createUIComponents() {
        File[] roots = File.listRoots();
        DefaultMutableTreeNode rootTreeNode;
        if (roots.length == 1) {
            rootTreeNode = new DefaultMutableTreeNode("/");
            tree = new JTree(rootTreeNode);
            treeModel = (DefaultTreeModel)tree.getModel();
            addChildFiles(roots[0], rootTreeNode);
            treeModel.reload();
        }//TODO write
    }
}
