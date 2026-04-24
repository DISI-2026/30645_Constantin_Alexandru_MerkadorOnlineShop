import React from 'react';
import { Link } from 'react-router-dom';

const NotFoundPage = () => (
    <div className="p-8 text-center max-w-md mx-auto mt-20">
        <h1 className="text-6xl font-extrabold text-red-500">404</h1>
        <p className="text-2xl text-gray-700 my-4">Page Not Found</p>
        <p className="text-gray-500 mb-8">
            The page you are looking for does not exist or you do not have permission to view it.
        </p>
        <Link
            to="/"
            className="px-6 py-3 bg-blue-500 text-white font-semibold rounded-lg hover:bg-blue-600 transition duration-150"
        >
            Go Home
        </Link>
    </div>
);

export default NotFoundPage;