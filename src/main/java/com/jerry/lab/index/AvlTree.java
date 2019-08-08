package com.jerry.lab.index;

import java.util.Arrays;

public class AvlTree extends IndexAdapter {
    public AvlNode root;

    public void insert(int key) {
        root = AvlNodeUtil.put(root, key);
    }

    public void delete(int key) {
        root = AvlNodeUtil.delete(root, key);
    }

    public int find(int key) {
        return AvlNodeUtil.get(root, key);
    }

    public boolean check() {
        if (!AvlNodeUtil.isBST(root)) {
            System.out.println("is not BST");
            return false;
        }

        if (!AvlNodeUtil.isBalance(root)) {
            System.out.println("is not balance");
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        AvlTree avlTree = new AvlTree();

        int[] keys = new int[]{2, 5, 7, 1, 4, 9};
        Arrays.stream(keys).forEachOrdered(key -> {
            System.out.println("insert " + key);
            avlTree.insert(key);
            AvlNodeUtil.print(avlTree.root);
        });

        System.out.println("get 3 " + avlTree.find(3));
        System.out.println("get 5 " + avlTree.find(5));
        System.out.println("get 9 " + avlTree.find(9));

        System.out.println("delete 5");
        avlTree.delete(5);
        AvlNodeUtil.print(avlTree.root);


    }

}

class AvlNodeUtil {

    public static AvlNode put(AvlNode node, int key) {
        CountUtil.addOperateCount();
        if (node == null) {
            return new AvlNode(key);
        }

        if (key < node.key) {// 插入左子树
            node.left = put(node.left, key);
        } else if (key > node.key) {// 插入右子树
            node.right = put(node.right, key);
        } else {// 已存在
            return node;
        }

        node = reBalance(node);
        refreshHeight(node);
        return node;
    }

    public static int get(AvlNode node, int key) {
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

    public static AvlNode delete(AvlNode node, int key) {
        CountUtil.addOperateCount();
        if (node == null) {
            return null;
        }

        if (key < node.key) {// 在左节点
            node.left = delete(node.left, key);
        } else if (key > node.key) {// 在右节点
            node.right = delete(node.right, key);
        } else {// 就在本节点，需要调整树结构
            if (node.left == null && node.right == null) {// 左右节点都不存在，直接删除本节点
                return null;
            } else if (node.right == null) {// 仅右节点不存在，将左节点提升
                node = node.left;
            } else if (node.left == null) {// 仅左节点不存在，将右节点提升
                node = node.right;
            } else {// 左右节点均存在，将右节点的最小值提升至本节点
                AvlNode temp = node;

                node = AvlNodeUtil.min(temp.right);
                node.right = AvlNodeUtil.deleteMin(temp.right);
                node.left = temp.left;
            }
        }

        node = reBalance(node);
        refreshHeight(node);

        return node;
    }

    private static AvlNode rotateWithLeftChild(AvlNode node) {
        CountUtil.addOperateCount(3);
        AvlNode leftNode = node.left;
        node.left = leftNode.right;
        leftNode.right = node;

        refreshHeight(node);
        refreshHeight(leftNode);
        return leftNode;
    }

    private static AvlNode rotateWithRightChild(AvlNode node) {
        CountUtil.addOperateCount(3);
        AvlNode rightNode = node.right;
        node.right = rightNode.left;
        rightNode.left = node;

        refreshHeight(node);
        refreshHeight(rightNode);
        return rightNode;
    }

    private static AvlNode doubleWithLeftChild(AvlNode node) {
        node.left = rotateWithRightChild(node.left);
        return rotateWithLeftChild(node);
    }

    private static AvlNode doubleWithRightChild(AvlNode node) {
        node.right = rotateWithLeftChild(node.right);
        return rotateWithRightChild(node);
    }

    public static void print(AvlNode node) {
        if (node != null) {
            System.out.println(node);
        }
    }

    private static void refreshHeight(AvlNode node) {
        CountUtil.addOperateCount();
        node.height = Math.max(height(node.left), height(node.right)) + 1;
    }

    private static AvlNode reBalance(AvlNode node) {
        CountUtil.addOperateCount();
        if (Math.abs(height(node.left) - height(node.right)) < 2) {
            return node;
        }

        if (height(node.left) > height(node.right)) {// 左子树过高，需要旋转
            if (height(node.left.left) > height(node.left.right)) {// 插入节点在外部，进行单旋转
                node = rotateWithLeftChild(node);
            } else {
                node = doubleWithLeftChild(node);
            }
        } else if (height(node.right) > height(node.left)) {// 右子树过高，需要旋转
            if (height(node.right.right) > height(node.right.left)) {// 插入节点在外部，进行单旋转
                node = rotateWithRightChild(node);
            } else {
                node = doubleWithRightChild(node);
            }
        }

        return node;
    }

    private static int height(AvlNode node) {
        return node == null ? -1 : node.height;
    }

    public static AvlNode deleteMin(AvlNode node) {
        CountUtil.addOperateCount();
        if (node == null) {
            return null;
        }

        if (node.left == null) {// 无左节点，则当前节点最小，将右节点提升
            return node.right;
        } else {// 有左节点，在左节点中删除
            return AvlNodeUtil.deleteMin(node.left);
        }
    }

    public static AvlNode min(AvlNode node) {
        CountUtil.addOperateCount();
        if (node == null) {
            return null;
        }

        if (node.left == null) {// 无左节点，则当前节点最小
            return node;
        } else {// 有左节点，在左节点中寻找
            return AvlNodeUtil.min(node.left);
        }
    }

    public static boolean isBST(AvlNode node) {
        return isBST(node, null, null);
    }

    private static boolean isBST(AvlNode node, Integer min, Integer max) {
        if (node == null) return true;
        if (min != null && node.key < min) return false;
        if (max != null && node.key > max) return false;
        return isBST(node.left, min, node.key) && isBST(node.right, node.key, max);
    }

    public static boolean isBalance(AvlNode node) {
        if (node == null) {
            return true;
        }

        if (Math.abs(height(node.right) - height(node.left)) < 2) {
            return isBalance(node.right) && isBalance(node.left);
        }

        return false;
    }
}

class AvlNode {
    AvlNode(int key) {
        this.key = key;
        height = 0;
    }

    int key;
    AvlNode left, right;
    int height;

    @Override
    public String toString() {
        if (height == 0) {
            return String.valueOf(key);
        }
        else {
            return key + "[" + height + ", " + (left != null ? left : "") + " , " + (right != null ? right : "") + "]";
        }

    }
}
