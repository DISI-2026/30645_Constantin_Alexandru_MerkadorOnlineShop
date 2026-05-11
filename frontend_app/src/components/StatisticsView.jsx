import React, { useState, useEffect } from 'react';
import { orderService } from '../api/orderService';
import { productService } from '../api/productService';
import { categoryService } from '../api/categoryService';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, Legend, ResponsiveContainer,
    PieChart, Pie, Cell, LineChart, Line
} from 'recharts';
import '../styles/StatisticsView.css';

const COLORS = ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#6f42c1', '#0dcaf0', '#fd7e14'];

// Util. function to extract the correct list from the response
const extractList = (response) => {
    if (Array.isArray(response)) return response;
    if (Array.isArray(response?.data)) return response.data;
    if (Array.isArray(response?.content)) return response.content;
    if (Array.isArray(response?.data?.content)) return response.data.content;
    return [];
};

const StatisticsView = () => {
    const [loading, setLoading] = useState(true);
    const [stats, setStats] = useState({
        kpis: { totalRevenue: 0, totalOrders: 0, avgOrderValue: 0, activeProducts: 0 },
        timelineData: [],
        categoryData: [],
        regionData: []
    });

    useEffect(() => {
        const fetchAndProcessData = async () => {
            try {
                setLoading(true);


                const [ordersRes, productsRes, categoriesRes] = await Promise.all([
                    orderService.getAllOrdersAdmin(),
                    productService.getProducts({ page: 0, size: 2000 }),
                    categoryService.getCategories()
                ]);

                const orders = extractList(ordersRes);
                const products = extractList(productsRes);
                const categories = extractList(categoriesRes);

                const categoryMap = {};
                categories.forEach(c => categoryMap[c.id] = c.name || c.slug);

                const productCategoryMap = {};
                products.forEach(p => {
                    productCategoryMap[p.id] = categoryMap[p.categoryId] || 'Unknown';
                });

                let totalRevenue = 0;
                let validOrdersCount = 0;

                const timelineMap = {};
                const categorySalesMap = {};
                const regionSalesMap = {};

                orders.forEach(order => {
                    // Ignore canceled orders in the totalRevenue calculation
                    if (order.status === 'CANCELLED') return;

                    validOrdersCount++;
                    const orderTotal = order.totalAmount || 0;
                    totalRevenue += orderTotal;

                    // --- Timeline processing ---
                    const date = new Date(order.placedAt);
                    const monthYear = date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });

                    if (!timelineMap[monthYear]) {
                        timelineMap[monthYear] = { name: monthYear, sales: 0, orders: 0 };
                    }
                    timelineMap[monthYear].sales += orderTotal;
                    timelineMap[monthYear].orders += 1;

                    // --- Regions processing---
                    const addressParts = order.deliveryAddress ? order.deliveryAddress.split(',') : [];
                    let region = addressParts.length > 1 ? addressParts[1].trim() : 'Unknown Area';

                    if (!regionSalesMap[region]) {
                        regionSalesMap[region] = { name: region, value: 0 };
                    }
                    regionSalesMap[region].value += orderTotal;

                    // --- Category processing ---
                    if (order.items && order.items.length > 0) {
                        order.items.forEach(item => {
                            const catName = productCategoryMap[item.productId] || 'Other Categories';
                            const itemTotal = item.unitPrice * item.quantity;

                            if (!categorySalesMap[catName]) {
                                categorySalesMap[catName] = { name: catName, value: 0 };
                            }
                            categorySalesMap[catName].value += itemTotal;
                        });
                    }
                });

                // Convert hashmaps to arrays for easier processing
                const timelineData = Object.values(timelineMap).sort((a, b) => new Date(a.name) - new Date(b.name));

                const categoryData = Object.values(categorySalesMap)
                    .sort((a, b) => b.value - a.value)
                    .map(item => ({ ...item, value: Number(item.value.toFixed(2)) })); // Rotunjire

                const regionData = Object.values(regionSalesMap)
                    .sort((a, b) => b.value - a.value)
                    .slice(0, 7) // Păstrăm doar top 7 regiuni pentru claritate
                    .map(item => ({ ...item, value: Number(item.value.toFixed(2)) }));

                // Set final stats
                setStats({
                    kpis: {
                        totalRevenue: totalRevenue.toFixed(2),
                        totalOrders: validOrdersCount,
                        avgOrderValue: validOrdersCount > 0 ? (totalRevenue / validOrdersCount).toFixed(2) : 0,
                        activeProducts: products.length
                    },
                    timelineData,
                    categoryData,
                    regionData
                });

            } catch (error) {
                console.error("Failed to load statistics:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchAndProcessData();
    }, []);

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '300px' }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading statistics...</span>
                </div>
            </div>
        );
    }

    return (
        <div className="stats-dashboard">
            {/* KPI Section */}
            <div className="kpi-container">
                <div className="kpi-card">
                    <h3 className="kpi-title">Total Revenue</h3>
                    <p className="kpi-value">${stats.kpis.totalRevenue}</p>
                </div>
                <div className="kpi-card">
                    <h3 className="kpi-title">Total Orders</h3>
                    <p className="kpi-value">{stats.kpis.totalOrders}</p>
                </div>
                <div className="kpi-card">
                    <h3 className="kpi-title">Avg. Order Value</h3>
                    <p className="kpi-value">${stats.kpis.avgOrderValue}</p>
                </div>
                <div className="kpi-card">
                    <h3 className="kpi-title">Listed Products</h3>
                    <p className="kpi-value">{stats.kpis.activeProducts}</p>
                </div>
            </div>

            {/* Charts Section */}
            <div className="charts-grid">

                {/* Sales Over Time (Line Chart) */}
                <div className="chart-card full-width">
                    <h3 className="chart-title">Revenue Over Time</h3>
                    <div className="chart-wrapper">
                        {stats.timelineData.length > 0 ? (
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={stats.timelineData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                    <XAxis dataKey="name" />
                                    <YAxis tickFormatter={(value) => `$${value}`} />
                                    <RechartsTooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
                                    <Legend />
                                    <Line type="monotone" dataKey="sales" name="Sales ($)" stroke="#0d6efd" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} />
                                </LineChart>
                            </ResponsiveContainer>
                        ) : (
                            <p className="text-muted text-center mt-5">No timeline data available.</p>
                        )}
                    </div>
                </div>

                {/* Category sales (PieChart) */}
                <div className="chart-card">
                    <h3 className="chart-title">Sales by Category</h3>
                    <div className="chart-wrapper">
                        {stats.categoryData.length > 0 ? (
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={stats.categoryData}
                                        cx="50%"
                                        cy="50%"
                                        labelLine={false}
                                        outerRadius={100}
                                        fill="#8884d8"
                                        dataKey="value"
                                        label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                                    >
                                        {stats.categoryData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <RechartsTooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
                                </PieChart>
                            </ResponsiveContainer>
                        ) : (
                            <p className="text-muted text-center mt-5">No category data available.</p>
                        )}
                    </div>
                </div>

                {/* Sales by Region (BarChart) */}
                <div className="chart-card">
                    <h3 className="chart-title">Top Regions by Revenue</h3>
                    <div className="chart-wrapper">
                        {stats.regionData.length > 0 ? (
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={stats.regionData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                                    <XAxis dataKey="name" />
                                    <YAxis tickFormatter={(value) => `$${value}`} />
                                    <RechartsTooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
                                    <Bar dataKey="value" name="Revenue ($)" fill="#198754" radius={[4, 4, 0, 0]}>
                                        {stats.regionData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[(index + 1) % COLORS.length]} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <p className="text-muted text-center mt-5">No region data available.</p>
                        )}
                    </div>
                </div>

            </div>
        </div>
    );
};

export default StatisticsView;