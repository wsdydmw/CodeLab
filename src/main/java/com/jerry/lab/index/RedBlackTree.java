package com.jerry.lab.index;

import java.util.Arrays;

public class RedBlackTree extends IndexAdapter{
    private RedBlackNode root;

    public int find(int key) {
        return RedBlackNodeUtil.get(root, key);
    }

    public void insert(int key) {
        root = RedBlackNodeUtil.put(root, key);
        root.color = Color.BLACK;
    }

    public void delete(int key) {
        root = RedBlackNodeUtil.delete(root, key);

    }


    public static void main(String[] args) {
        RedBlackTree redBlackTree = new RedBlackTree();

        int[] keys = new int[]{2, 5, 7, 1, 4, 9};
        Arrays.stream(keys).forEachOrdered(key -> {
            System.out.println("insert " + key);
            redBlackTree.insert(key);
            RedBlackNodeUtil.print(redBlackTree.root);
            System.out.println();
        });

        System.out.println("get 3 " + redBlackTree.find(3));
        System.out.println("get 5 " + redBlackTree.find(5));
        System.out.println("get 9 " + redBlackTree.find(9));

        System.out.println("delete 5 ");
        redBlackTree.delete(5);
        RedBlackNodeUtil.print(redBlackTree.root);
    }

    public boolean check() {
        return false;
    }
}

class RedBlackNodeUtil {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    public static RedBlackNode put(RedBlackNode node, int key) {
        if (node == null) return new RedBlackNode(key);

        if (key < node.key) node.left = put(node.left, key);
        else if (key > node.key) node.right = put(node.right, key);
        else ;

        // fix-up any right-leaning links
        if (!isRed(node.left) && isRed(node.right)) node = rotateLeft(node);
        if (isRed(node.left) && isRed(node.left.left)) node = rotateRight(node);
        if (isRed(node.left) && isRed(node.right)) flipColors(node);

        return node;
    }

    public static int get(RedBlackNode node, int key) {
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

    public static RedBlackNode delete(RedBlackNode h, int key) {
        assert contains(h, key);

        if (key < h.key) {
            if (!isRed(h.left) && !isRed(h.left.left))
                h = moveRedLeft(h);
            h.left = delete(h.left, key);
        } else {
            if (isRed(h.left))
                h = rotateRight(h);
            if (key == h.key && (h.right == null))
                return null;
            if (!isRed(h.right) && !isRed(h.right.left))
                h = moveRedRight(h);
            if (key == h.key) {
                h.key = min(h.right).key;
                h.right = deleteMin(h.right);
            } else h.right = delete(h.right, key);
        }
        return balance(h);
    }

    private static boolean contains(RedBlackNode node, int key) {
        return (get(node, key) != -1);
    }

    // Assuming that h is red and both h.left and h.left.left
    // are black, make h.left or one of its children red.
    private static RedBlackNode moveRedLeft(RedBlackNode h) {
        assert (h != null);
        assert isRed(h) && !isRed(h.left) && !isRed(h.left.left);

        flipColors(h);
        if (isRed(h.right.left)) {
            h.right = rotateRight(h.right);
            h = rotateLeft(h);
            // flipColors(h);
        }
        return h;
    }

    // Assuming that h is red and both h.right and h.right.left
    // are black, make h.right or one of its children red.
    private static RedBlackNode moveRedRight(RedBlackNode h) {
        assert (h != null);
        assert isRed(h) && !isRed(h.right) && !isRed(h.right.left);
        flipColors(h);
        if (isRed(h.left.left)) {
            h = rotateRight(h);
            // flipColors(h);
        }
        return h;
    }

    // restore red-black tree invariant
    private static RedBlackNode balance(RedBlackNode h) {
        assert (h != null);

        if (isRed(h.right)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);

        return h;
    }

    // the smallest key in subtree rooted at x; null if no such key
    private static RedBlackNode min(RedBlackNode x) {
        assert x != null;
        if (x.left == null) return x;
        else return min(x.left);
    }

    private static RedBlackNode deleteMin(RedBlackNode h) {
        if (h.left == null)
            return null;

        if (!isRed(h.left) && !isRed(h.left.left))
            h = moveRedLeft(h);

        h.left = deleteMin(h.left);
        return balance(h);
    }

    private static boolean isRed(RedBlackNode x) {
        if (x == null) return false;
        return (x.color == Color.RED);
    }

    private static RedBlackNode rotateRight(RedBlackNode h) {
        System.out.println("rotateRight " + h.key);
        //assert (h != null) && isRed(h.left);
        RedBlackNode x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = x.right.color;
        x.right.color = Color.RED;
        return x;
    }

    private static RedBlackNode rotateLeft(RedBlackNode node) {
        System.out.println("rotateLeft " + node.key);
        //assert (node != null) && isRed(node.right);
        RedBlackNode temp = node.right;
        node.right = temp.left;
        temp.left = node;
        temp.color = node.color;
        node.color = Color.RED;
        return temp;
    }

    private static void flipColors(RedBlackNode node) {
        System.out.println("flip color for " + node.key);
        // h must have opposite color of its two children
        /*assert (h != null) && (h.left != null) && (h.right != null);
        assert (!isRed(h) && isRed(h.left) && isRed(h.right))
                || (isRed(h) && !isRed(h.left) && !isRed(h.right));*/
        node.color = node.color.opposite();
        node.left.color = node.left.color.opposite();
        node.right.color = node.right.color.opposite();
    }

    /*private static int size(RedBlackNode x) {
        if (x == null) return 0;
        return x.N;
    }*/

    public static void print(RedBlackNode redBlackNode) {
        if (redBlackNode != null) {
            System.out.print(redBlackNode);
        }
    }

}

class RedBlackNode {
    int key;
    RedBlackNode left, right;
    Color color;

    public RedBlackNode(int key) {
        this.key = key;
        this.color = Color.RED;
    }

    @Override
    public String toString() {
        return key + "[" + color + ", " + (left != null ? left : "") + " , " + (right != null ? right : "") + "]";

    }
}

enum Color {
    RED, BLACK;

    public Color opposite() {
        if (this.equals(RED)) {
            return BLACK;
        } else {
            return RED;
        }
    }
}
