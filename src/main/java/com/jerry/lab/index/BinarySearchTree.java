package com.jerry.lab.index;

public class BinarySearchTree extends IndexAdapter {
    private Node root;

    public int find(int key) {
        return NodeUtil.get(root, key);
    }

    public void insert(int key) {
        root = NodeUtil.put(root, key);
    }

    public void delete(int key) {
        root = NodeUtil.delete(root, key);
    }

    public boolean check() {
        return NodeUtil.isBST(root);
    }

    public static void main(String[] args) {
        BinarySearchTree binarySearchTree = new BinarySearchTree();
        binarySearchTree.insert(2);
        binarySearchTree.insert(5);
        binarySearchTree.insert(7);
        binarySearchTree.insert(1);
        binarySearchTree.insert(4);
        binarySearchTree.insert(9);

        NodeUtil.print(binarySearchTree.root);

        System.out.println();
        System.out.println("get 4 " + binarySearchTree.find(4));
        System.out.println("get 5 " + binarySearchTree.find(5));
        System.out.println("get 9 " + binarySearchTree.find(9));

        binarySearchTree.delete(7);
        binarySearchTree.delete(6);
        NodeUtil.print(binarySearchTree.root);
    }
}

class NodeUtil {

    /**
     * 与查找过程类似，如果待插入键不存在，则创建新节点。否则，根据待插入键的大小与当前结点的键的大小关系，选择在左子树或者右子树中继续插入。
     */
    public static Node put(Node node, int key) {
        CountUtil.addOperateCount();
        if (node == null) {
            return new Node(key);
        }

        if (key < node.key) {// 放到左边
            if (node.left == null) {
                node.left = new Node(key);
            } else {
                put(node.left, key);
            }
        } else if (key > node.key) {// 放到右边
            if (node.right == null) {
                node.right = new Node(key);
            } else {
                put(node.right, key);
            }
        }

        return node;
    }

    /**
     * 采用递归方式进行查找，如果被查找键和当前结点的键相等，则查找命中。否则，根据被查找键与当前节点的键的大小关系在左子树或右子树中继续查找，直至命中或节点不存在。
     */
    public static int get(Node node, int key) {
        CountUtil.addOperateCount();
        if (node == null) {
            return -1;
        }

        if (key < node.key) {// 在左边寻找
            return get(node.left, key);
        } else if (key > node.key) {// 在右边寻找
            return get(node.right, key);
        } else {// 在当前节点
            return key;
        }
    }

    /**
     * 采用递归方式进行删除，如果待删除键为当前键，则需要改变当前节点结构。否则，根据待删除键与当前节点的键的大小关系在左子树或右子树中继续删除，直至命中或节点不存在。
     */
    public static Node delete(Node node, int key) {
        CountUtil.addOperateCount();
        if (node == null) {
            return node;
        }

        if (key < node.key) {// 在左节点
            node.left = delete(node.left, key);
        } else if (key > node.key) {// 在右节点
            node.right = delete(node.right, key);
        } else {// 就在本节点，需要调整树结构
            if (node.left == null && node.right == null) {// 左右节点都不存在，直接删除本节点
                return null;
            } else if (node.right == null) {// 仅右节点不存在，将左节点提升
                return node.left;
            } else if (node.left == null) {// 仅左节点不存在，将右节点提升
                return node.right;
            } else {// 左右节点均存在，将右节点的最小值提升至本节点
                Node temp = node;

                node = NodeUtil.min(node.right);
                node.right = NodeUtil.deleteMin(temp);
                node.left = temp.left;

                return node;
            }
        }

        return node;
    }

    public static Node deleteMin(Node node) {
        CountUtil.addOperateCount();
        if (node == null) {
            return null;
        }

        if (node.left == null) {// 无左节点，则当前节点最小，将右节点提升
            return node.right;
        } else {// 有左节点，在左节点中删除
            return NodeUtil.deleteMin(node.left);
        }
    }

    public static Node min(Node node) {
        CountUtil.addOperateCount();
        if (node == null) {
            return null;
        }

        if (node.left == null) {// 无左节点，则当前节点最小
            return node;
        } else {// 有左节点，在左节点中寻找
            return NodeUtil.min(node.left);
        }
    }


    public static void print(Node node) {
        if (node == null) {
            return;
        }

        if (node.left == null) {
            NodeUtil.print(node.left);
        }
        System.out.print(node.key + ", ");
        if (node.right != null) {
            NodeUtil.print(node.right);
        }
    }

    public static boolean isBST(Node node) {
        return isBST(node, null, null);
    }

    private static boolean isBST(Node node, Integer min, Integer max) {
        if (node == null) return true;
        if (min != null && node.key < min) return false;
        if (max != null && node.key > max) return false;
        return isBST(node.left, min, node.key) && isBST(node.right, node.key, max);
    }
}

class Node {
    int key;
    Node left, right;

    public Node(Integer key) {
        this.key = key;
    }
}


